package com.turkcell.roaming.roaming_assistant.business.concretes;

import com.turkcell.roaming.roaming_assistant.business.abstracts.RecommendationService;
import com.turkcell.roaming.roaming_assistant.business.abstracts.SimulationService;
import com.turkcell.roaming.roaming_assistant.dto.requests.RecommendationRequest;
import com.turkcell.roaming.roaming_assistant.dto.requests.SimulateRequest;
import com.turkcell.roaming.roaming_assistant.dto.responses.*;
import org.springframework.stereotype.Service; import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class RecommendationManager implements RecommendationService {

    private final SimulationService simulationService;

    @Override
    public RationaleDto recommend(RecommendationRequest req) {
        SimulateResponse sim = simulationService.simulate(new SimulateRequest(req.user_id(), req.trips(), req.profile()));

        // Scoring: compare by total_cost_try (TRY normalize). Lower is better.
        List<OptionDto> candidates = sim.options().stream()
                .filter(o -> "payg".equals(o.kind()) || "pack".equals(o.kind()))
                .collect(Collectors.toList());

        candidates.sort(Comparator.comparing((OptionDto o) -> (o.total_cost_try()==null? Double.MAX_VALUE : o.total_cost_try())));

        List<TopOptionDto> top3 = new ArrayList<>();
        for(OptionDto o: candidates.stream().limit(3).toList()){
            String label;
            String explanation;
            String details;

            if("payg".equals(o.kind())){
                label = "Yalnız Pay-as-you-go";
                explanation = "Paket alınmadı; tüm günler tekil ücretlendirildi.";
                details = "Aşım riski yok; ancak toplam maliyet genelde daha yüksek olur.";
            } else {
                label = o.pack_name() + (o.n_packs()>1? (" x"+o.n_packs()) : "");
                // Uyarılar
                List<String> warn = new ArrayList<>();
                if(!o.validity_ok()) warn.add("Validity kısa → çoklu paket önerildi.");
                if(!o.coverage_hit()) warn.add("Kapsama yok.");
                explanation = String.join(" ", warn.isEmpty()? List.of("Kapsama ve validity uygun.") : warn);
                details = "Paket kapsamındaki günler paketten; kapsam dışı günler tekil ücretle hesaplandı. Aşım varsa PAYG oranlarıyla çarpıldı.";
            }

            top3.add(new TopOptionDto(label, o.total_cost(), o.currency(), o.total_cost_try(), explanation, details));
        }

        String rationale = """
    Sıralama TRY normalize edilerek yapılmıştır (mock kur). Eşitlikte kapsam genişliği (region > country) ve validity uyumu tercih edilir.
    Multi-country rotada kapsanan günler pakete, kalan günler PAYG'a yazılır. Aşım (GB/dk/SMS) varsa ülke PAYG oranlarıyla maliyete eklenir.
    """;

        return new RationaleDto(top3, rationale);
    }
}