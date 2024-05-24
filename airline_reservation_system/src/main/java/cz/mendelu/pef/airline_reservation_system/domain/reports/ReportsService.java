package cz.mendelu.pef.airline_reservation_system.domain.reports;

import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightRepository;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ReportsService {

    private FlightRepository flightRepository;

    private FlightService flightService;

    private AirportService airportService;

    public ReportsService(
            FlightRepository flightRepository,
            FlightService flightService,
            AirportService airportService
    ) {
        this.flightRepository = flightRepository;
        this.flightService = flightService;
        this.airportService = airportService;
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

    public double calculatePassengerLoadFactor(List<Flight> flights) {
        List<Flight> flightsWithTickets = flights
                .stream()
                .filter(f -> !f.getTickets().isEmpty())
                .toList();

        // Passenger load factor is the ratio of passenger-kilometers travelled to seat-kilometers available (Wikipedia)
        double passengerKilometers = 0;
        double seatKilometers = 0;

        for (Flight flight : flightsWithTickets) {
            // Passengers
            var occupiedSeatsTotalNumber = flightService
                    .getOccupiedSeats(flight)
                    .values()
                    .stream()
                    .map(List::size)
                    .mapToLong(l -> l)
                    .sum();
            // Seats
            var allSeatsTotalNumber = flight.getAircraft().getTotalCapacity();
            var travelledDistance = airportService.calculateDistanceBetweenAirports(
                    flight.getAirportDeparture(),
                    flight.getAirportArrival()
            );

            passengerKilometers += travelledDistance * occupiedSeatsTotalNumber;
            seatKilometers += travelledDistance * allSeatsTotalNumber;
        }

        if (seatKilometers == 0) {
            return 0.0;
        }

        return passengerKilometers / seatKilometers * 100;
    }

    public Reports getAllReports(OffsetDateTime startDate, OffsetDateTime endDate) {
        var flights = fetchFlightsOverDatePeriod(startDate, endDate);
        var tickets = flights
                .stream()
                .map(Flight::getTickets)
                .flatMap(Set::stream)
                .toList();

        return new Reports(
                getTicketSales(tickets),
                getTicketClassDistribution(tickets),
                flightRepository.getTop5FlightIdsByTicketSales(startDate, endDate),
                getCancelledAndDelayedFlights(flights),
                calculatePassengerLoadFactor(flights)
        );
    }

    private List<Flight> fetchFlightsOverDatePeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        return flightRepository.getFlightsByDepartureGreaterThanEqualAndArrivalLessThan(startDate, endDate);
    }
}
