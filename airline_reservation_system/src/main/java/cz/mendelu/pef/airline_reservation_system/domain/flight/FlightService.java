package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    private FlightRepository flightRepository;

    FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<Flight> getAllFlights(Pageable pageRequest) {
        return flightRepository
                .findAll(pageRequest)
                .getContent();
    }

    /**
     *
     * @param id - id of existing fare tariff
     * @return list of all flights associating with the fare tariff. NOTE: it's not paginated.
     */
    public List<Flight> getAllFlightsByFareTariffId(Long id) {
        List<Flight> flights = new ArrayList<>();
        flightRepository.getFlightsByFareTariff_IdEquals(id).forEach(flights::add);

        return flights;
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public Flight createFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Long id, Flight flight) {
        flight.setId(id);
        return flightRepository.save(flight);
    }

    public void deleteFlightById(Long id) {
        flightRepository.deleteById(id);
    }

    public void setFareTariff(FareTariff fareTariff, List<Flight> flights) {
        flights.forEach(fl -> fl.setFareTariff(fareTariff));
        flightRepository.saveAll(flights);
    }
}
