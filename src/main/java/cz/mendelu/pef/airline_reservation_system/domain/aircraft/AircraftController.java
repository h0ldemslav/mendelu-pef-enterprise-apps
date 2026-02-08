package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

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
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("aircrafts")
@Validated
public class AircraftController {

    private AircraftService aircraftService;

    @Autowired
    AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @Operation(summary = "Get all aircrafts")
    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<AircraftResponse> getAircrafts() {
        return ArrayResponse.of(
                aircraftService.getAllAircrafts(),
                AircraftResponse::new
        );
    }

    @Operation(summary = "Get one aircraft by id")
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
    public ObjectResponse<AircraftResponse> getAircraftById(@PathVariable Long id) {
        Aircraft aircraft = aircraftService
                .getAircraftById(id)
                .orElseThrow(NotFoundException::new);

        return ObjectResponse.of(
                aircraft,
                AircraftResponse::new
        );
    }

    @Operation(summary = "Create new aircraft")
    @PostMapping(value = "", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Valid
    public ObjectResponse<AircraftResponse> createAircraft(@RequestBody @Valid AircraftRequest request) {
        Aircraft aircraft = new Aircraft();
        request.toAircraft(aircraft);

        aircraftService.createAircraft(aircraft);

        return ObjectResponse.of(
                aircraft,
                AircraftResponse::new
        );
    }

    @Operation(summary = "Update one aircraft by id")
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
    public ObjectResponse<AircraftResponse> updateAircraftById(
            @PathVariable Long id,
            @RequestBody @Valid AircraftRequest request
    ) {
        Aircraft aircraft = aircraftService
                .getAircraftById(id)
                .orElseThrow(NotFoundException::new);
        request.toAircraft(aircraft);

        aircraftService.updateAircraft(id, aircraft);

        return ObjectResponse.of(
                aircraft,
                AircraftResponse::new
        );
    }

    @Operation(summary = "Delete one aircraft by id")
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAircraftById(@PathVariable Long id) {
        aircraftService.deleteAircraftById(id);
    }
}
