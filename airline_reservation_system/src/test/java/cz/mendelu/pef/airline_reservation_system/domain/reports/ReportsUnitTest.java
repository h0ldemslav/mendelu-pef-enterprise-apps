package cz.mendelu.pef.airline_reservation_system.domain.reports;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.airport.Airport;
import cz.mendelu.pef.airline_reservation_system.domain.airport.AirportService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReportsUnitTest {

    @Test
    public void testCalculatePassengerLoadFactor() {
        // given
        FlightService flightService = new FlightService(null);
        AirportService airportService = new AirportService(null);
        ReportsService reportsService = new ReportsService(null, flightService, airportService);

        // when
        double result1 = reportsService.calculatePassengerLoadFactor(getFlightsForTesting());
        double result2 = reportsService.calculatePassengerLoadFactor(List.of());

        // then
        assertThat(result1, is(5.434782608695652));
        assertThat(result2, is(0.0));
    }

    @Test
    public void testGetTicketSales() {
        // given
        ReportsService reportsService = new ReportsService(null, null, null);

        List<Ticket> tickets = new ArrayList<>();
        getFlightsForTesting()
                .stream()
                .map(Flight::getTickets)
                .forEach(tickets::addAll);

        // when
        double result = reportsService.getTicketSales(tickets);

        // then
        assertThat(result, is(55545.0));
    }

    @Test
    public void testGetTicketClassDistribution() {
        // given
        ReportsService reportsService = new ReportsService(null, null, null);

        List<Ticket> tickets = new ArrayList<>();
        getFlightsForTesting()
                .stream()
                .map(Flight::getTickets)
                .forEach(tickets::addAll);

        // when
        Map<String, Long> ticketClassDistribution = reportsService.getTicketClassDistribution(tickets);

        // then
        assertThat(ticketClassDistribution.get(TicketClass.Economy.name()), is(5L));
        assertThat(ticketClassDistribution.get(TicketClass.Premium.name()), is(0L));
        assertThat(ticketClassDistribution.get(TicketClass.Business.name()), is(5L));
    }

    @Test
    public void testGetCancelledAndDelayedFlights() {
        // given
        FlightService flightService = new FlightService(null);
        AirportService airportService = new AirportService(null);
        ReportsService reportsService = new ReportsService(null, flightService, airportService);

        // when
        Map<String, Long> cancelledAndDelayedFlights = reportsService.getCancelledAndDelayedFlights(getFlightsForTesting());

        // then
        assertThat(cancelledAndDelayedFlights.get("Delayed"), is(1L));
        assertThat(cancelledAndDelayedFlights.get("Cancelled"), is(0L));
    }

    private static List<Flight> getFlightsForTesting() {
        List<String> seatNumberLetters = List.of("A", "B", "C", "D", "E", "F");

        Aircraft aircraft = new Aircraft();
        aircraft.setEconomyCapacity(50);
        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(30);

        Airport airport1 = new Airport();
        airport1.setLatitude(31.255053);
        airport1.setLongitude(-81.466932);

        Airport airport2 = new Airport();
        airport2.setLatitude(47.491676);
        airport2.setLongitude(21.609334);

        Flight flight1 = new Flight();
        flight1.setId(1L);
        flight1.setNumber("AA0718");
        flight1.setDeparture(OffsetDateTime.parse("2017-07-16T09:35:00+01:00"));
        flight1.setArrival(OffsetDateTime.parse("2017-07-16T12:35:00+01:00"));
        flight1.setStatus("Scheduled");
        flight1.setAircraft(aircraft);
        flight1.setAirportDeparture(airport1);
        flight1.setAirportArrival(airport2);

        Set<Ticket> flight1Tickets = flight1.getTickets();

        IntStream.range(0, 5).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i + 1));
            ticket.setTicketClass(TicketClass.Economy);
            ticket.setSeatNumber(12 + seatNumberLetters.get(i));
            ticket.setPrice(722.0);
            ticket.setDiscount(0.0);
            ticket.setPriceAfterDiscount(722.0);

            flight1Tickets.add(ticket);
        });

        Flight flight2 = new Flight();
        flight2.setId(2L);
        flight2.setNumber("BB1399");
        flight2.setDeparture(OffsetDateTime.parse("2017-07-19T12:00:00+01:00"));
        flight2.setArrival(OffsetDateTime.parse("2017-07-19T14:35:00+01:00"));
        flight2.setStatus("Delayed");
        flight2.setDelay(10);
        flight2.setAircraft(aircraft);
        flight2.setAirportDeparture(airport2);
        flight2.setAirportArrival(airport1);

        Set<Ticket> flight2Tickets = flight2.getTickets();

        IntStream.range(0, 5).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i + 1));
            ticket.setTicketClass(TicketClass.Business);
            ticket.setSeatNumber(1 + seatNumberLetters.get(i));
            ticket.setPrice(10387.0);
            ticket.setDiscount(0.0);
            ticket.setPriceAfterDiscount(10387.0);

            flight2Tickets.add(ticket);
        });

        airport1.getDepartureFlights().add(flight1);
        airport1.getArrivalFlights().add(flight2);
        airport2.getDepartureFlights().add(flight2);
        airport2.getArrivalFlights().add(flight1);

        return List.of(flight1, flight2);
    }
}
