package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FareTariffResponse {

    private Long id;

    @Schema(example = "AD17")
    @NotEmpty
    private String code;

    @JsonProperty("business_price")
    @Schema(example = "13021.00")
    @NotNull
    private Double businessPrice;

    @JsonProperty("premium_price")
    @Schema(example = "4757.00")
    @NotNull
    private Double premiumPrice;

    @JsonProperty("economy_price")
    @Schema(example = "475.00")
    @NotNull
    private Double economyPrice;

    public FareTariffResponse(FareTariff fareTariff) {
        this.id = fareTariff.getId();
        this.code = fareTariff.getCode();
        this.businessPrice = fareTariff.getBusinessPrice();
        this.premiumPrice = fareTariff.getPremiumPrice();
        this.economyPrice = fareTariff.getEconomyPrice();
    }
}
