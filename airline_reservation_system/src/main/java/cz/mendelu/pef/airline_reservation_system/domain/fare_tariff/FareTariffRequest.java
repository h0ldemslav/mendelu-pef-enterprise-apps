package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FareTariffRequest {

    @Schema(example = "AD17")
    @NotEmpty
    private String code;

    @JsonProperty("business_price")
    @Schema(example = "13021.00")
    @NotNull
    @Positive
    private Double businessPrice;

    @JsonProperty("premium_price")
    @Schema(example = "4757.00")
    @NotNull
    @Positive
    private Double premiumPrice;

    @JsonProperty("economy_price")
    @Schema(example = "475.00")
    @NotNull
    @Positive
    private Double economyPrice;

    public void toFareTariff(FareTariff fareTariff) {
        fareTariff.setCode(this.code);
        fareTariff.setBusinessPrice(this.businessPrice);
        fareTariff.setPremiumPrice(this.premiumPrice);
        fareTariff.setEconomyPrice(this.economyPrice);
    }
}
