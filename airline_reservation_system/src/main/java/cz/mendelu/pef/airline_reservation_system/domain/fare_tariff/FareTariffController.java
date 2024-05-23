package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.MissingFareTariffReplacementException;
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
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("fare_tariffs")
@Validated
public class FareTariffController {

    private FareTariffService fareTariffService;
    private FlightService flightService;

    @Autowired
    FareTariffController(FareTariffService fareTariffService, FlightService flightService) {
        this.fareTariffService = fareTariffService;
        this.flightService = flightService;
    }

    @Operation(summary = "Get all fare tariffs")
    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<FareTariffResponse> getFareTariffs() {
        return ArrayResponse.of(
                fareTariffService.getAllFareTariffs(),
                FareTariffResponse::new
        );
    }

    @Operation(summary = "Get one fare tariff by id")
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
    public ObjectResponse<FareTariffResponse> getFareTariffById(@PathVariable Long id) {
        FareTariff fareTariff = fareTariffService
                .getFareTariffById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                fareTariff,
                FareTariffResponse::new
        );
    }

    @Operation(summary = "Create new fare tariff")
    @PostMapping(value = "", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Valid
    public ObjectResponse<FareTariffResponse> createFareTariff(@RequestBody @Valid FareTariffRequest request) {
        FareTariff fareTariff = new FareTariff();
        request.toFareTariff(fareTariff);

        fareTariffService.createFareTariff(fareTariff);

        return ObjectResponse.of(
                fareTariff,
                FareTariffResponse::new
        );
    }

    @Operation(summary = "Update one fare tariff by id")
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
    public ObjectResponse<FareTariffResponse> updateFareTariffById(
            @PathVariable Long id,
            @RequestBody @Valid FareTariffRequest request
    ) {
        FareTariff fareTariff = fareTariffService
                .getFareTariffById(id)
                .orElseThrow(NotFoundException::new);
        request.toFareTariff(fareTariff);

        fareTariffService.updateFareTariff(id, fareTariff);

        return ObjectResponse.of(
                fareTariff,
                FareTariffResponse::new
        );
    }

    @Operation(summary = "Delete one fare tariff by id")
    @DeleteMapping(value = "")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Missing fare tariff replacement; if fare tariff is used by at least one flight, the replacement must be provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorDetails.class)
                    )
            )
    })
    public void deleteFareTariffById(
            @RequestParam Long id,
            @Parameter(
                    description = "Fare tariff replacement id must be provided, if fare tariff is used by at least one flight")
            @RequestParam(required = false) Long replacementId
    ) {
        List<Flight> flights = flightService.getAllFlightsByFareTariffId(id);

        if (!flights.isEmpty()) {
            if (replacementId == null) {
                throw new MissingFareTariffReplacementException();
            }

            FareTariff fareTariffReplacement = fareTariffService
                    .getFareTariffById(replacementId)
                    .orElseThrow(NotFoundException::new);

            flightService.setFareTariff(fareTariffReplacement, flights);
        }

        fareTariffService.deleteFareTariffById(id);
    }
}
