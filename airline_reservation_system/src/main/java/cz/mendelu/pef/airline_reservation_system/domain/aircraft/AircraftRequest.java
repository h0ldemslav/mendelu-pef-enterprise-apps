package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AircraftRequest {

    @NotEmpty
    private String code;

    @NotEmpty
    private String model;

    @JsonProperty("business_capacity")
    @NotNull
    private Integer businessCapacity;

    @JsonProperty("premium_capacity")
    @NotNull
    private Integer premiumCapacity;

    @JsonProperty("economy_capacity")
    @NotNull
    private Integer economyCapacity;

    public void toAircraft(Aircraft aircraft) {
        aircraft.setCode(this.code);
        aircraft.setModel(this.model);
        aircraft.setBusinessCapacity(this.businessCapacity);
        aircraft.setPremiumCapacity(this.premiumCapacity);
        aircraft.setEconomyCapacity(this.economyCapacity);
    }
}
