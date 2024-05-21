package cz.mendelu.pef.airline_reservation_system.domain.airport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AirportRequest {

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

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    public void toAirport(Airport airport) {
        airport.setCode(this.code);
        airport.setName(this.name);
        airport.setCountryCode(this.countryCode);
        airport.setRegionCode(this.regionCode);
        airport.setMunicipality(this.municipality);
        airport.setGpsCode(this.gpsCode);
        airport.setLatitude(this.latitude);
        airport.setLongitude(this.longitude);
    }
}
