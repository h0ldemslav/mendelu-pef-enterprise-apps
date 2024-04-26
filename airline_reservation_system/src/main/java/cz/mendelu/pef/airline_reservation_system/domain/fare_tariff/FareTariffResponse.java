package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FareTariffResponse {

    private Long id;

    @NotEmpty
    private String code;

    @JsonProperty("business_price")
    @NotNull
    private Double businessPrice;

    @JsonProperty("premium_price")
    @NotNull
    private Double premiumPrice;

    @JsonProperty("economy_price")
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
