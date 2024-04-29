package cz.mendelu.pef.airline_reservation_system.domain.flight;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FlightResponse {

    private Long id;

    @NotEmpty
    private String number;

    @NotNull
    private OffsetDateTime departure;

    @NotNull
    private  OffsetDateTime arrival;

    @NotEmpty
    private String status;

    private Integer delay;

    @JsonProperty("aircraft_id")
    private Long aircraftId;

    @JsonProperty("airport_departure_id")
    @NotNull
    private Long airportDepartureId;

    @JsonProperty("airport_arrival_id")
    @NotNull
    private Long airportArrivalId;

    @JsonProperty("fare_tariff_id")
    @NotNull
    private Long fareTariffId;

    FlightResponse(Flight flight) {
        this.id = flight.getId();
        this.number = flight.getNumber();
        this.departure = flight.getDeparture();
        this.arrival = flight.getArrival();
        this.status = flight.getStatus();
        this.delay = flight.getDelay();
        this.airportDepartureId = flight.getAirportDeparture().getId();
        this.airportArrivalId = flight.getAirportArrival().getId();
        this.fareTariffId = flight.getFareTariff().getId();

        if (flight.getAircraft() != null) {
            this.aircraftId = flight.getAircraft().getId();
        }
    }
}
