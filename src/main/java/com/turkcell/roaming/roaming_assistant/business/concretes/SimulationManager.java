package com.turkcell.roaming.roaming_assistant.business.concretes;

import com.turkcell.roaming.roaming_assistant.business.abstracts.CurrencyService;
import com.turkcell.roaming.roaming_assistant.business.abstracts.SimulationService;
import com.turkcell.roaming.roaming_assistant.dto.requests.SimulateRequest;
import com.turkcell.roaming.roaming_assistant.dto.requests.TripInput;
import com.turkcell.roaming.roaming_assistant.dto.responses.*;
import com.turkcell.roaming.roaming_assistant.exception.BadRequestException;
import com.turkcell.roaming.roaming_assistant.exception.NotFoundException;
import com.turkcell.roaming.roaming_assistant.model.entity.*;
import com.turkcell.roaming.roaming_assistant.repository.*;
import com.turkcell.roaming.roaming_assistant.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class SimulationManager implements SimulationService {

    private final CountryRepository countryRepo;
    private final RoamingRateRepository rateRepo;
    private final RoamingPackRepository packRepo;
    private final UserRepository userRepo;
    private final CurrencyService currencyService;

    @Override
    public SimulateResponse simulate(SimulateRequest req) {
        AppUser user = userRepo.findById(req.user_id()).orElseThrow(() -> new NotFoundException("User not found: "+req.user_id()));
        if(req.trips()==null || req.trips().isEmpty()) throw new BadRequestException("Trips empty");

        // Normalize trips -> country + days
        record TripCalc(Country c, RoamingRate rate, int days, TripInput input) {}
        List<TripCalc> tcs = new ArrayList<>();
        for(TripInput ti: req.trips()){
            if(ti.end_date().isBefore(ti.start_date())) throw new BadRequestException("end_date before start_date");
            Country c = countryRepo.findById(ti.country_code()).orElseThrow(() -> new NotFoundException("Country "+ti.country_code()+" not found"));
            RoamingRate r = rateRepo.findByCountryCode(ti.country_code()).orElseThrow(() -> new NotFoundException("Rate for "+ti.country_code()+" not found"));
            int days = DateUtil.daysInclusive(ti.start_date(), ti.end_date());
            tcs.add(new TripCalc(c, r, days, ti));
        }

        int totalDays = tcs.stream().mapToInt(TripCalc::days).sum();

        // Expected total usage
        double needGb = (req.profile().avg_daily_mb() * totalDays) / 1024.0;
        int needMin = req.profile().avg_daily_min() * totalDays;
        int needSms = req.profile().avg_daily_sms() * totalDays;

        SummaryDto summary = new SummaryDto(totalDays, round2(needGb), needMin, needSms);

        // PAYG cost (pure)
        // sum per trip with that country's currency -> one currency? dataset uses EUR for DE/GR, USD for US/AE/EG
        // We'll compute per currency bucket, but API wants one "payg" option; en sade hali: eğer çoklu currency varsa text'e yazalım.
        // Burada MVP için: sadece tek currency olan route'larda payg tek döner; farklıysa "currency" = MIX
        String paygCurrency = null;
        double paygTotal = 0;
        Map<String, Double> paygByCurrency = new LinkedHashMap<>();
        for(TripCalc t: tcs){
            double tripMb = req.profile().avg_daily_mb() * t.days();
            double tripMin = req.profile().avg_daily_min() * t.days();
            double tripSms = req.profile().avg_daily_sms() * t.days();
            double cost = tripMb * t.rate.getDataPerMb() + tripMin * t.rate.getVoicePerMin() + tripSms * t.rate.getSmsPerMsg();
            paygByCurrency.merge(t.rate.getCurrency(), cost, Double::sum);
        }
        if(paygByCurrency.size()==1){
            paygCurrency = paygByCurrency.keySet().iterator().next();
            paygTotal = paygByCurrency.get(paygCurrency);
        } else {
            // convert to TRY for single comparable number
            paygCurrency = "MIX";
            for(var e: paygByCurrency.entrySet()){
                Double tryEq = currencyService.toTRY(e.getKey(), e.getValue());
                paygTotal += (tryEq==null?0:tryEq);
            }
        }
        OptionDto paygOption = new OptionDto(
                "payg", null, null, 0, false, true,
                new OverflowBreakdown(0,0,0,0,0,0),
                round2(paygTotal), paygCurrency, ("MIX".equals(paygCurrency)? round2(paygTotal): currencyService.toTRY(paygCurrency, paygTotal))
        );

        // PACK options
        // For each pack, compute covered days (sum of trips where pack covers region/country/global)
        List<OptionDto> allOptions = new ArrayList<>();
        allOptions.add(paygOption);

        // Build helper for weighted avg rate over covered trips (for overage)
        // Map currency->weighted rate set impossible; instead overage computed in each country's currency then converted to pack currency via TRY normalization to compare.
        // For simplicity: compute overage costs per trip in that trip's payg currency; convert to pack currency via TRY baseline.
        for(RoamingPack p: packRepo.findAll()){
            // Determine covered trips
            List<TripCalc> covered = tcs.stream().filter(tc -> covers(p, tc.c())).toList();
            int coveredDays = covered.stream().mapToInt(TripCalc::days).sum();
            boolean coverageHit = coveredDays>0;
            if(!coverageHit){
                // still can list as "pack + all uncovered payg" but it's meaningless; skip packs with 0 coverage
                continue;
            }

            // n_packs by validity
            int nPacks = (int) Math.ceil(coveredDays / (double) p.getValidityDays());
            boolean validityOk = p.getValidityDays() >= coveredDays; // bilgi amaçlı

            // usage for covered & uncovered
            double coveredGb = (req.profile().avg_daily_mb() * coveredDays) / 1024.0;
            int coveredMin = req.profile().avg_daily_min() * coveredDays;
            int coveredSms = req.profile().avg_daily_sms() * coveredDays;

            List<TripCalc> uncovered = tcs.stream().filter(tc -> !covers(p, tc.c())).toList();

            // base price in pack currency
            double base = nPacks * p.getPrice();

            // overage over 'covered' usage
            double overGb = Math.max(0.0, coveredGb - nPacks * p.getDataGb());
            double overMin = Math.max(0.0, coveredMin - nPacks * p.getVoiceMin());
            double overSms = Math.max(0.0, coveredSms - nPacks * p.getSms());

            // cost of overage: use PAYG rates of the actual countries (weighted by days)
            // compute in TRY then convert back to pack currency if possible, otherwise leave TRY equivalent for comparison only.
            double overCostTRY = 0;
            if(overGb>0 || overMin>0 || overSms>0){
                // distribute proportionally by days among covered trips
                int sumDays = coveredDays==0?1:coveredDays;
                for(TripCalc tc: covered){
                    double share = tc.days()/(double)sumDays;
                    double shareGb = overGb*share;
                    double shareMin = overMin*share;
                    double shareSms = overSms*share;
                    double tripCost = shareGb*1024*tc.rate.getDataPerMb() + shareMin*tc.rate.getVoicePerMin() + shareSms*tc.rate.getSmsPerMsg();
                    Double tryEq = currencyService.toTRY(tc.rate.getCurrency(), tripCost);
                    if(tryEq!=null) overCostTRY += tryEq;
                }
            }

            // uncovered days pure PAYG
            double uncoveredTRY = 0;
            for(TripCalc tc: uncovered){
                double tripMb = req.profile().avg_daily_mb() * tc.days();
                double tripMin = req.profile().avg_daily_min() * tc.days();
                double tripSms = req.profile().avg_daily_sms() * tc.days();
                double cost = tripMb*tc.rate.getDataPerMb() + tripMin*tc.rate.getVoicePerMin() + tripSms*tc.rate.getSmsPerMsg();
                Double tryEq = currencyService.toTRY(tc.rate.getCurrency(), cost);
                if(tryEq!=null) uncoveredTRY += tryEq;
            }

            // Convert base to TRY, sum, then also keep pack currency number for display
            Double baseTRY = currencyService.toTRY(p.getCurrency(), base);
            double totalTRY = (baseTRY==null?0:baseTRY) + overCostTRY + uncoveredTRY;

            // For the response "total_cost" prefer pack currency if single-currency world; otherwise return TRY
            // Here pack currency is single => we expose pack currency total as base + (over/uncovered converted back approx? keep TRY as separate)
            OptionDto od = new OptionDto(
                    "pack", p.getPackId(), p.getName(), nPacks,
                    coverageHit, validityOk,
                    new OverflowBreakdown(round2(overGb), round2(overMin), round2(overSms), round2(overCostTRY), 0,0),
                    round2(base), p.getCurrency(), round2(totalTRY)
            );
            allOptions.add(od);
        }

        return new SimulateResponse(summary, allOptions);
    }

    private boolean covers(RoamingPack p, Country c){
        if(p.getCoverageType()==null) return false;
        return switch (p.getCoverageType()){
            case REGION -> "Global".equalsIgnoreCase(p.getCoverage()) || p.getCoverage().equalsIgnoreCase(c.getRegion());
            case COUNTRY -> p.getCoverage().equalsIgnoreCase(c.getCountryCode());
        };
    }

    private static double round2(double v){ return Math.round(v*100.0)/100.0; }
}