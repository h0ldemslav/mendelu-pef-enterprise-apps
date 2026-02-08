package cz.mendelu.pef.airline_reservation_system.domain.airport;

import cz.mendelu.pef.airline_reservation_system.utils.exceptions.AirportInUseException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.helpers.ApiErrorDetails;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("airports")
@Validated
public class AirportController {

    private AirportService airportService;

    @Autowired
    AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @Operation(summary = "Get all airports")
    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<AirportResponse> getAirports(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit
    ) {
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = limit != null && limit > 0 ? limit : 100;
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize);

        return ArrayResponse.of(
                airportService.getAllAirports(pageRequest),
                AirportResponse::new
        );
    }


    @Operation(summary = "Get one airport by id")
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
    public ObjectResponse<AirportResponse> getAirportById(@PathVariable Long id) {
        Airport airport = airportService
                .getAirportById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                airport,
                AirportResponse::new
        );
    }

    @Operation(summary = "Create new airport")
    @PostMapping(value = "", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Valid
    public ObjectResponse<AirportResponse> createAirport(@RequestBody @Valid AirportRequest request) {
        Airport airport = new Airport();
        request.toAirport(airport);

        airportService.createAirport(airport);

        return ObjectResponse.of(
                airport,
                AirportResponse::new
        );
    }

    @Operation(summary = "Update one airport by id")
    @PutMapping(value = "/{id}", produces = "application/json")
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
    public ObjectResponse<AirportResponse> updateAirportById(
            @PathVariable Long id,
            @RequestBody @Valid AirportRequest request
    ) {
        Airport airport = airportService
                .getAirportById(id)
                .orElseThrow(NotFoundException::new);
        request.toAirport(airport);

        airportService.updateAirport(id, airport);

        return ObjectResponse.of(
                airport,
                AirportResponse::new
        );
    }

    @Operation(summary = "Delete one airport by id")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAirportById(@PathVariable Long id) {
        airportService.getAirportById(id).ifPresent(a -> {
            if (!a.getDepartureFlights().isEmpty() || !a.getArrivalFlights().isEmpty()) {
                throw new AirportInUseException();
            }
        });

        airportService.deleteAirportById(id);
    }
}
