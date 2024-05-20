package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AircraftService {

    private AircraftRepository aircraftRepository;

    AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public List<Aircraft> getAllAircrafts() {
        List<Aircraft> aircrafts = new ArrayList<>();
        aircraftRepository.findAll().forEach(aircrafts::add);

        return aircrafts;
    }

    public Optional<Aircraft> getAircraftById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return aircraftRepository.findById(id);
    }

    public Aircraft createAircraft(Aircraft aircraft) {
        return aircraftRepository.save(aircraft);
    }

    public Aircraft updateAircraft(Long id, Aircraft aircraft) {
        aircraft.setId(id);
        return aircraftRepository.save(aircraft);
    }

    public void deleteAircraftById(Long id) {
        aircraftRepository.deleteById(id);
    }
}
