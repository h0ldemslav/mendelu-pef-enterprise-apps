package cz.mendelu.pef.airline_reservation_system.domain.flight;

import org.springframework.data.repository.CrudRepository;

public interface FlightRepository extends CrudRepository<Flight, Long> {
    Iterable<Flight> getFlightsByFareTariff_IdEquals(Long id);
}
