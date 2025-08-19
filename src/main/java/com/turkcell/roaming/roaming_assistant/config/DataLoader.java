package com.turkcell.roaming.roaming_assistant.config;

import com.opencsv.CSVReader;
import com.turkcell.roaming.roaming_assistant.model.CoverageType;
import com.turkcell.roaming.roaming_assistant.model.entity.*;
import com.turkcell.roaming.roaming_assistant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CountryRepository countryRepo;
    private final RoamingRateRepository rateRepo;
    private final RoamingPackRepository packRepo;
    private final UserRepository userRepo;
    private final UsageProfileRepository profRepo;
    private final TripRepository tripRepo;

    @Override
    public void run(String... args) throws Exception {
        if (countryRepo.count() > 0) return; // zaten seed edilmişse çık

        // countries
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/countries.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                String code = row[0];
                if (!countryRepo.existsById(code)) {
                    countryRepo.save(Country.builder()
                            .countryCode(code)
                            .countryName(row[1])
                            .region(row[2])
                            .build());
                }
            }
        }

        // rates
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/roaming_rates.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                Country c = countryRepo.findById(row[0]).orElseThrow();
                // country + currency eşsiz kabul edelim
                if (rateRepo.findByCountryAndCurrency(c, row[4]).isEmpty()) {
                    rateRepo.save(RoamingRate.builder()
                            .country(c)
                            .dataPerMb(Double.parseDouble(row[1]))
                            .voicePerMin(Double.parseDouble(row[2]))
                            .smsPerMsg(Double.parseDouble(row[3]))
                            .currency(row[4])
                            .build());
                }
            }
        }

        // packs
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/roaming_packs.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                long packId = Long.parseLong(row[0]);
                if (!packRepo.existsById(packId)) {
                    packRepo.save(RoamingPack.builder()
                            .packId(packId)
                            .name(row[1])
                            .coverage(row[2])
                            .coverageType(CoverageType.valueOf(row[3].toUpperCase()))
                            .dataGb(Double.parseDouble(row[4]))
                            .voiceMin(Integer.parseInt(row[5]))
                            .sms(Integer.parseInt(row[6]))
                            .price(Double.parseDouble(row[7]))
                            .validityDays(Integer.parseInt(row[8]))
                            .currency(row[9])
                            .build());
                }
            }
        }

        // users
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/users.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                long userId = Long.parseLong(row[0]);
                if (!userRepo.existsById(userId)) {
                    userRepo.save(AppUser.builder()
                            .userId(userId)
                            .name(row[1])
                            .homePlan(row[2])
                            .build());
                }
            }
        }

        // profiles
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/usage_profile.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                long userId = Long.parseLong(row[0]);
                AppUser u = userRepo.findById(userId).orElseThrow();
                // her kullanıcı için 1 profile
                if (profRepo.findByUser(u).isEmpty()) {
                    profRepo.save(UsageProfile.builder()
                            .user(u)
                            .avgDailyMb(Integer.parseInt(row[1]))
                            .avgDailyMin(Integer.parseInt(row[2]))
                            .avgDailySms(Integer.parseInt(row[3]))
                            .build());
                }
            }
        }

        // trips
        try (var r = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/trips.csv").getInputStream()))) {
            String[] row; r.readNext();
            while ((row = r.readNext()) != null) {
                long tripId = Long.parseLong(row[0]);
                if (!tripRepo.existsById(tripId)) {
                    AppUser u = userRepo.findById(Long.parseLong(row[1])).orElseThrow();
                    Country c = countryRepo.findById(row[2]).orElseThrow();
                    tripRepo.save(Trip.builder()
                            .tripId(tripId)
                            .user(u)
                            .country(c)
                            .startDate(LocalDate.parse(row[3]))
                            .endDate(LocalDate.parse(row[4]))
                            .build());
                }
            }
        }
    }
}
