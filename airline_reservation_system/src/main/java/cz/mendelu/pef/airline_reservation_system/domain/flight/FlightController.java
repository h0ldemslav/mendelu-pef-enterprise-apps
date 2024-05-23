package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.AircraftService;
import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariffService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidFlightException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.helpers.ApiErrorDetails;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

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

    @Operation(summary = "Get all flights")
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

    @Operation(summary = "Get one flight by id")
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
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

    @Operation(summary = "Get flight available seats by id")
    @GetMapping(value = "/{id}/seat_numbers", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{ \"Business\": [], \"Premium\": [], \"Economy\": [] }")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            ),
            @ApiResponse(
                  responseCode = "422",
                  description = "Cannot get seats, aircraft is null",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ApiErrorDetails.class)
                  )
            )
    })
    @Valid
    public Map<String, List<String>> getAvailableSeatNumbersById(@PathVariable Long id) {
        Flight flight = flightService
                .getFlightById(id)
                .orElseThrow(NotFoundException::new);

        try {
            return flightService.getAvailableSeats(flight);
        } catch (InvalidFlightException e) {
            // Flight aircraft could be null
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getDetail(), e);
        }
    }

    @Operation(summary = "Create new flight")
    @PostMapping(value = "", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passed aircraft/airport/fare tariff is not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
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

    @Operation(summary = "Update one flight by id")
    @PutMapping(value = "/{id}", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found or aircraft/airport/fare tariff is null",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
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

    @Operation(summary = "Cancel one flight by id")
    @PutMapping(value = "/cancel/{id}", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Id not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    @Valid
    public ObjectResponse<FlightResponse> cancelFlight(
            @PathVariable Long id,
            @Parameter(description = "Ticket discount percentage for cancelling flight; will be applied to each ticket of this flight")
            @RequestParam(name = "ticket_discount_percentage") Double ticketDiscountPercentage
    ) {
        Flight flight = flightService
                .getFlightById(id)
                .orElseThrow(NotFoundException::new);
        flightService.cancelFlight(flight, ticketDiscountPercentage);
        flightService.updateFlight(id, flight);

        return ObjectResponse.of(
                flight,
                FlightResponse::new
        );
    }

    @Operation(summary = "Delete one flight by id")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlightById(@PathVariable Long id) {
        flightService.deleteFlightById(id);
    }
}
