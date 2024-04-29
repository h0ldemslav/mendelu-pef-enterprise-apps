package cz.mendelu.pef.airline_reservation_system.domain.flight;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.domain.aircraft.AircraftService;
import cz.mendelu.pef.airline_reservation_system.domain.airport.Airport;
import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariffService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FlightRequest {

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
