package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FareTariffService {

    private FareTariffRepository fareTariffRepository;

    FareTariffService(FareTariffRepository fareTariffRepository) {
        this.fareTariffRepository = fareTariffRepository;
    }

    public List<FareTariff> getAllFareTariffs() {
        List<FareTariff> fareTariffs = new ArrayList<>();
        fareTariffRepository.findAll().forEach(fareTariffs::add);

        return fareTariffs;
    }

    public Optional<FareTariff> getFareTariffById(Long id) {
        return fareTariffRepository.findById(id);
    }

    public FareTariff createFareTariff(FareTariff fareTariff) {
        return fareTariffRepository.save(fareTariff);
    }

    public FareTariff updateFareTariff(Long id, FareTariff fareTariff) {
        fareTariff.setId(id);
        return fareTariffRepository.save(fareTariff);
    }

    public void deleteFareTariffById(Long id) {
        fareTariffRepository.deleteById(id);
    }
}
