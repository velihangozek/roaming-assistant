package com.turkcell.roaming.roaming_assistant;

import com.turkcell.roaming.roaming_assistant.dto.requests.SimulateRequest;
import com.turkcell.roaming.roaming_assistant.dto.requests.TripInput;
import com.turkcell.roaming.roaming_assistant.dto.responses.SimulateResponse;
import com.turkcell.roaming.roaming_assistant.business.abstracts.SimulationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SimulationManagerTest {

    @Autowired SimulationService simulationService;

    @Test void europeTripShouldPrefer10GbPack(){
        var req = new SimulateRequest(
                1001L,
                List.of(
                        new TripInput("DE", LocalDate.parse("2025-08-20"), LocalDate.parse("2025-08-25")),
                        new TripInput("GR", LocalDate.parse("2025-08-26"), LocalDate.parse("2025-08-28"))
                ),
                new SimulateRequest.Profile(600,10,2)
        );
        SimulateResponse res = simulationService.simulate(req);
        assertThat(res.summary().total_days()).isEqualTo(9);
        // 10GB tek paket base 29.9 EUR bekleriz; 5GB x2 ise 39.8 EUR
        var pack10 = res.options().stream().filter(o -> "pack".equals(o.kind()) && "Avrupa 10GB".equals(o.pack_name())).findFirst().orElseThrow();
        assertThat(pack10.n_packs()).isEqualTo(1);
        assertThat(pack10.total_cost()).isEqualTo(29.9);
    }
}