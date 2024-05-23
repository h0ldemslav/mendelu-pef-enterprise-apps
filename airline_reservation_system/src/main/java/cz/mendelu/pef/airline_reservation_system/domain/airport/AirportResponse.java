package cz.mendelu.pef.airline_reservation_system.domain.airport;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AirportResponse {

    private Long id;

    @Schema(example = "BQK")
    @NotEmpty
    private String code;

    @Schema(example = "Brunswick Golden Isles Airport")
    @NotEmpty
    private String name;

    @Schema(example = "US")
    @JsonProperty("country_code")
    @NotEmpty
    private String countryCode;

    @Schema(example = "US-GA")
    @JsonProperty("region_code")
    @NotEmpty
    private String regionCode;

    @Schema(example = "Brunswick")
    private String municipality;

    @Schema(example = "KBQK")
    @JsonProperty("gps_code")
    private String gpsCode;

    @Schema(example = "11.980569")
    @NotNull
    private Double latitude;

    @Schema(example = "-86.311052")
    @NotNull
    private Double longitude;

    AirportResponse(Airport airport) {
        this.id = airport.getId();
        this.code = airport.getCode();
        this.name = airport.getName();
        this.countryCode = airport.getCountryCode();
        this.regionCode = airport.getRegionCode();
        this.municipality = airport.getMunicipality();
        this.gpsCode = airport.getGpsCode();
        this.latitude = airport.getLatitude();
        this.longitude = airport.getLongitude();
    }
}
