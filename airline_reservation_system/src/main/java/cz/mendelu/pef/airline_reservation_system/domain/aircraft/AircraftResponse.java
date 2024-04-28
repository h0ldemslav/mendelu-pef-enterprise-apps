package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AircraftResponse {

    private Long id;

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

    AircraftResponse(Aircraft aircraft) {
        this.id = aircraft.getId();
        this.code = aircraft.getCode();
        this.model = aircraft.getModel();
        this.businessCapacity = aircraft.getBusinessCapacity();
        this.premiumCapacity = aircraft.getPremiumCapacity();
        this.economyCapacity = aircraft.getEconomyCapacity();
    }
}
