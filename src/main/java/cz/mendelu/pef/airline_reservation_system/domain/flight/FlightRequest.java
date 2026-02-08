package cz.mendelu.pef.airline_reservation_system.domain.flight;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.domain.aircraft.AircraftService;
import cz.mendelu.pef.airline_reservation_system.domain.airport.Airport;
import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariffService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class FlightRequest {

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

    public void toFlight(
            Flight flight,
            AircraftService aircraftService,
            AirportService airportService,
            FareTariffService fareTariffService
    ) {
        flight.setNumber(this.number);
        flight.setDeparture(this.departure);
        flight.setArrival(this.arrival);
        flight.setStatus(this.status);
        flight.setDelay(this.delay);

        Airport airportDeparture = airportService
                .getAirportById(this.airportDepartureId)
                .orElseThrow(NotFoundException::new);
        airportDeparture.getDepartureFlights().add(flight);

        Airport airportArrival = airportService
                .getAirportById(this.airportArrivalId)
                .orElseThrow(NotFoundException::new);
        airportArrival.getArrivalFlights().add(flight);

        flight.setAirportDeparture(airportDeparture);
        flight.setAirportArrival(airportArrival);

        FareTariff fareTariff = fareTariffService
                .getFareTariffById(this.fareTariffId)
                .orElseThrow(NotFoundException::new);
        flight.setFareTariff(fareTariff);

        aircraftService
                .getAircraftById(this.aircraftId)
                .ifPresent(a -> {
                    a.getFlights().add(flight);
                    flight.setAircraft(a);
                });
    }
}
