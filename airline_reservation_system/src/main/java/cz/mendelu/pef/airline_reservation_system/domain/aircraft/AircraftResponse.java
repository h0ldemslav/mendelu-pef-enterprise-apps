package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AircraftResponse {

    private Long id;

    @Schema(example = "FFE12")
    @NotEmpty
    private String code;

    @Schema(example = "Boeing 767")
    @NotEmpty
    private String model;

    @JsonProperty("business_capacity")
    @Schema(example = "12")
    @NotNull
    private Integer businessCapacity;

    @JsonProperty("premium_capacity")
    @Schema(example = "50")
    @NotNull
    private Integer premiumCapacity;

    @JsonProperty("economy_capacity")
    @Schema(example = "98")
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
