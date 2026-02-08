package cz.mendelu.pef.airline_reservation_system.domain.flight;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Override
    @EntityGraph(attributePaths = {"tickets"})
    @NonNull
    Page<Flight> findAll(@NonNull Pageable pageable);

    Iterable<Flight> getFlightsByFareTariff_IdEquals(Long id);

    @EntityGraph(attributePaths = {"tickets"})
    @NonNull
    List<Flight> getFlightsByDepartureGreaterThanEqualAndArrivalLessThan(OffsetDateTime startDate, OffsetDateTime endDate);

    @Query("""
        SELECT subquery.flight_id FROM (
            SELECT t.flight.id AS flight_id, SUM(t.priceAfterDiscount) AS ticket_sales
            FROM Ticket AS t
            WHERE t.flight.departure >= :startDate AND t.flight.arrival < :endDate
            GROUP BY flight_id
            ORDER BY ticket_sales DESC
            LIMIT 5
        ) AS subquery
    """)
    List<Long> getTop5FlightIdsByTicketSales(OffsetDateTime startDate, OffsetDateTime endDate);
}
