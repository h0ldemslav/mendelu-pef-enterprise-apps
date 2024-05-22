package cz.mendelu.pef.airline_reservation_system.domain.airport;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AirportService {

    private AirportRepository airportRepository;

    AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<Airport> getAllAirports(Pageable pageRequest) {
        return airportRepository
                .findAll(pageRequest)
                .getContent();
    }

    public Optional<Airport> getAirportById(Long id) {
        return airportRepository.findById(id);
    }

    public Airport createAirport(Airport airport) {
        return airportRepository.save(airport);
    }

    public Airport updateAirport(Long id, Airport airport) {
        airport.setId(id);
        return airportRepository.save(airport);
    }

    public void deleteAirportById(Long id) {
        airportRepository.deleteById(id);
    }

    public double calculateDistanceBetweenAirports(Airport origin, Airport destination) {
        if (origin == null || destination == null) {
            return 0.0;
        }

        double originLat = origin.getLatitude();
        double originLong = origin.getLongitude();
        double destinationLat = destination.getLatitude();
        double destinationLong = destination.getLongitude();

        final int EARTH_RADIUS_IN_KM = 6371;

        // Haversine formula
        double latDistance = Math.toRadians(destinationLat - originLat);
        double longDistance = Math.toRadians(destinationLong - originLong);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(originLat)) * Math.cos(Math.toRadians(destinationLat))
                * Math.sin(longDistance / 2) * Math.sin(longDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_IN_KM * c;
    }
}