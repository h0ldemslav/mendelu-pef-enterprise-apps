package cz.mendelu.pef.airline_reservation_system.domain.airport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AirportResponse {

    private Long id;

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @JsonProperty("country_code")
    @NotEmpty
    private String countryCode;

    @JsonProperty("region_code")
    @NotEmpty
    private String regionCode;

    private String municipality;

    @JsonProperty("gps_code")
    private String gpsCode;

    AirportResponse(Airport airport) {
        this.id = airport.getId();
        this.code = airport.getCode();
        this.name = airport.getName();
        this.countryCode = airport.getCountryCode();
        this.regionCode = airport.getRegionCode();
        this.municipality = airport.getMunicipality();
        this.gpsCode = airport.getGpsCode();
    }
}
