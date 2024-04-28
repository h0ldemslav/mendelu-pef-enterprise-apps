package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
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

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<AircraftResponse> getAircrafts() {
        return ArrayResponse.of(
                aircraftService.getAllAircrafts(),
                AircraftResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
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

    @PostMapping(value = "", produces = "application/json")
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

    @PutMapping(value = "/{id}", produces = "application/json")
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

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAircraftById(@PathVariable Long id) {
        aircraftService.deleteAircraftById(id);
    }
}
