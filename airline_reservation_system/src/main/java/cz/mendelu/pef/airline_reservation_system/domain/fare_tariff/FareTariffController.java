package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import cz.mendelu.pef.airline_reservation_system.utils.response.ArrayResponse;
import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("fare_tariffs")
@Validated
public class FareTariffController {

    private FareTariffService fareTariffService;

    @Autowired
    FareTariffController(FareTariffService fareTariffService) {
        this.fareTariffService = fareTariffService;
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

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFareTariffById(@PathVariable Long id) {
        fareTariffService.deleteFareTariffById(id);
    }
}
