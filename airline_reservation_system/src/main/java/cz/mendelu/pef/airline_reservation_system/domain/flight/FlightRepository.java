package cz.mendelu.pef.airline_reservation_system.domain.flight;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Iterable<Flight> getFlightsByFareTariff_IdEquals(Long id);
}
