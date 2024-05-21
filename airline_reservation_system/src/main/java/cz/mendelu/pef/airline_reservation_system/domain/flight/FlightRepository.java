package cz.mendelu.pef.airline_reservation_system.domain.flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Iterable<Flight> getFlightsByFareTariff_IdEquals(Long id);

    List<Flight> getFlightsByDepartureGreaterThanAndArrivalLessThan(OffsetDateTime startDate, OffsetDateTime endDate);

    @Query("""
        SELECT subquery.flight_id FROM (
            SELECT t.flight.id AS flight_id, SUM(t.priceAfterDiscount) AS ticket_sales
            FROM Ticket AS t
            GROUP BY flight_id
            ORDER BY ticket_sales DESC
            LIMIT 5
        ) AS subquery
    """)
    List<Long> getTop5FlightIdsByTicketSales();
}
