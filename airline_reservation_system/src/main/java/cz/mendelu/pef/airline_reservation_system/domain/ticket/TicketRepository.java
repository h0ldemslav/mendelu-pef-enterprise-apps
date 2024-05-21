package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> getTicketsByDepartureGreaterThanAndArrivalLessThan(OffsetDateTime startDate, OffsetDateTime endDate);
}
