package cz.mendelu.pef.airline_reservation_system.domain.flight;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FlightResponse {

    private Long id;

    @Schema(example = "PG0405")
    @NotEmpty
    private String number;

    @Schema(example = "2017-08-16T09:25:00Z")
    @NotNull
    private OffsetDateTime departure;

    @Schema(example = "2017-08-16T13:25:00Z")
    @NotNull
    private  OffsetDateTime arrival;

    @Schema(example = "Scheduled")
    @NotEmpty
    private String status;

    @Schema(description = "Flight delay in minutes", example = "null")
    private Integer delay;

    @JsonProperty("aircraft_id")
    @Schema(example = "1")
    private Long aircraftId;

    @JsonProperty("airport_departure_id")
    @Schema(example = "1")
    @NotNull
    private Long airportDepartureId;

    @JsonProperty("airport_arrival_id")
    @Schema(example = "2")
    @NotNull
    private Long airportArrivalId;

    @JsonProperty("fare_tariff_id")
    @Schema(example = "1")
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
