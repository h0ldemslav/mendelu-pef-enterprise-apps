package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.MissingFareTariffReplacementException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
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

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ArrayResponse<FareTariffResponse> getFareTariffs() {
        return ArrayResponse.of(
                fareTariffService.getAllFareTariffs(),
                FareTariffResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
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

    @PutMapping(value = "/{id}", produces = "application/json")
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

    @DeleteMapping(value = "")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFareTariffById(@RequestParam Long id, @RequestParam(required = false) Long replacementId) {
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
