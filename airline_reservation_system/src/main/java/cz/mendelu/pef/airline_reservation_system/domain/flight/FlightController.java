package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.AircraftService;
import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariffService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("flights")
@Validated
public class FlightController {

    private FlightService flightService;
    private AircraftService aircraftService;
    private AirportService airportService;
    private FareTariffService fareTariffService;

    @Autowired
    FlightController(
            FlightService flightService,
            AircraftService aircraftService,
            AirportService airportService,
            FareTariffService fareTariffService
    ) {
        this.flightService = flightService;
        this.aircraftService = aircraftService;
        this.airportService = airportService;
        this.fareTariffService = fareTariffService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<FlightResponse> getFlights(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = limit != null && limit > 0 ? limit : 100;
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize);

        return ArrayResponse.of(
                flightService.getAllFlights(pageRequest),
                FlightResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<FlightResponse> getFlightById(@PathVariable Long id) {
        Flight flight = flightService
                .getFlightById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                flight,
                FlightResponse::new
        );
    }

    @PostMapping(value = "", produces = "application/json")
    @Valid
    public ObjectResponse<FlightResponse> createFlight(@RequestBody @Valid FlightRequest request) {
        Flight flight = new Flight();
        request.toFlight(flight, aircraftService, airportService, fareTariffService);

        flightService.createFlight(flight);

        return ObjectResponse.of(
                flight,
                FlightResponse::new
        );
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @Valid
    public ObjectResponse<FlightResponse> updateFlightById(
            @PathVariable Long id,
            @RequestBody @Valid FlightRequest request
    ) {
        Flight flight = flightService
                .getFlightById(id)
                .orElseThrow(NotFoundException::new);
        request.toFlight(flight, aircraftService, airportService, fareTariffService);

        flightService.updateFlight(id, flight);

        return ObjectResponse.of(
                flight,
                FlightResponse::new
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlightById(@PathVariable Long id) {
        flightService.deleteFlightById(id);
    }
}
