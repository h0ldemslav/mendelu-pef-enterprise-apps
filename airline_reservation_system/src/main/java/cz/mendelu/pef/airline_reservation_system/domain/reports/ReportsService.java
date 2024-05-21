package cz.mendelu.pef.airline_reservation_system.domain.reports;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightRepository;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.TicketRepository;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ReportsService {

    private TicketRepository ticketRepository;

    private FlightRepository flightRepository;

    public ReportsService(TicketRepository ticketRepository, FlightRepository flightRepository) {
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
    }

    public double getTicketSales(List<Ticket> tickets) {
        return tickets
                .stream()
                .map(Ticket::getPriceAfterDiscount)
                .mapToDouble(d -> d)
                .sum();
    }

    public Map<String, Long> getTicketClassDistribution(List<Ticket> tickets) {
        Map<String, Long> ticketClassDistribution = new HashMap<>();

        for (TicketClass ticketClass : TicketClass.values()) {
            var ticketClassName = ticketClass.name();
            var distribution = tickets
                    .stream()
                    .filter(t -> t.getTicketClass().name().equals(ticketClassName))
                    .count();
            ticketClassDistribution.put(ticketClassName, distribution);
        }

        return ticketClassDistribution;
    }

    public Map<String, Long> getCancelledAndDelayedFlights(List<Flight> flights) {
        Map<String, Long> cancelledAndDelayedFlights = new HashMap<>();

        for (String status : List.of("Cancelled", "Delayed")) {
            var numberWithThisStatus = flights
                    .stream()
                    .filter(f -> f.getStatus().equalsIgnoreCase(status))
                    .count();
            cancelledAndDelayedFlights.put(status, numberWithThisStatus);
        }

        return cancelledAndDelayedFlights;
    }

    public Reports getAllReports(OffsetDateTime startDate, OffsetDateTime endDate) {
        var tickets = fetchTicketsOverDatePeriod(startDate, endDate);
        var flights = fetchFlightsOverDatePeriod(startDate, endDate);

        return new Reports(
                getTicketSales(tickets),
                getTicketClassDistribution(tickets),
                new ArrayList<>(),
                0L,
                getCancelledAndDelayedFlights(flights)
        );
    }

    private List<Ticket> fetchTicketsOverDatePeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return new ArrayList<>(ticketRepository.findAll())
                .stream()
                .filter(t -> t.getDeparture().isAfter(startDate) && t.getArrival().isBefore(endDate))
                .toList();
    }

    private List<Flight> fetchFlightsOverDatePeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return new ArrayList<>(flightRepository.findAll())
                .stream()
                .filter(f -> f.getDeparture().isAfter(startDate) && f.getArrival().isBefore(endDate))
                .toList();
    }
}
