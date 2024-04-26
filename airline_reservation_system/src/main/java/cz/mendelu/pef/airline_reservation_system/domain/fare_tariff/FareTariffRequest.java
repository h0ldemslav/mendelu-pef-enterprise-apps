package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FareTariffRequest {

    @NotEmpty
    private String code;

    @JsonProperty("business_price")
    @NotNull
    @Positive
    private Double businessPrice;

    @JsonProperty("premium_price")
    @NotNull
    @Positive
    private Double premiumPrice;

    @JsonProperty("economy_price")
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
