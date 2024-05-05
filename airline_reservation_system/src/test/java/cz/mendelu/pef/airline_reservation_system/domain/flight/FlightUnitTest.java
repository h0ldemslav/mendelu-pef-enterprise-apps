package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class FlightUnitTest {

    @Test
    public void testGetSeatNumber_VeryFirstSeat() {
        // given
        var flightService = new FlightService(null);

        var aircraft = new Aircraft();
        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(23);
        aircraft.setEconomyCapacity(79);

        var flight = new Flight();
        flight.setAircraft(aircraft);

        // when
        var businessTicketSeat = flightService.getSeatNumber(flight, TicketClass.Business).orElseThrow();
        var premiumTicketSeat = flightService.getSeatNumber(flight, TicketClass.Premium).orElseThrow();
        var economyTicketSeat = flightService.getSeatNumber(flight, TicketClass.Economy).orElseThrow();

        // then
        assertEquals(businessTicketSeat, "1A");
        assertEquals(premiumTicketSeat, "3A");
        assertEquals(economyTicketSeat, "7A");
    }

    @Test
    public void testGetSeatNumber_WithSomeSeatsAlreadyOccupied() {
        // given
        var flightService = new FlightService(null);

        var aircraft = new Aircraft();
        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(50);
        aircraft.setEconomyCapacity(90);

        var flight = new Flight();
        flight.setAircraft(aircraft);

        IntStream.range(0, 3).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Business.name());
            ticket.setSeatNumber(flightService.getSeatNumber(flight, TicketClass.Business).orElseThrow());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 6).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium.name());
            ticket.setSeatNumber(flightService.getSeatNumber(flight, TicketClass.Premium).orElseThrow());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy.name());
            ticket.setSeatNumber(flightService.getSeatNumber(flight, TicketClass.Economy).orElseThrow());

            flight.getTickets().add(ticket);
        });

        // when
        var businessTicketSeat = flightService.getSeatNumber(flight, TicketClass.Business).orElseThrow();
        var premiumTicketSeat = flightService.getSeatNumber(flight, TicketClass.Premium).orElseThrow();
        var economyTicketSeat = flightService.getSeatNumber(flight, TicketClass.Economy).orElseThrow();

        // then
        assertEquals(businessTicketSeat, "1D");
        assertEquals(premiumTicketSeat, "4A");
        assertEquals(economyTicketSeat, "20C");
    }

    @Test
    public void testIsTicketClassSeatsAvailable() {
        // given
        var flightService = new FlightService(null);
        var aircraft = new Aircraft();
        var flight = new Flight();

        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(50);
        aircraft.setEconomyCapacity(90);
        flight.setAircraft(aircraft);

        IntStream.range(0, 2).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Business.name());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 10).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium.name());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy.name());

            flight.getTickets().add(ticket);
        });

        // then
        assertTrue(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Business));
        assertTrue(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Premium));
        assertTrue(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Economy));
    }

    @Test
    public void testIsTicketClassSeatsAvailable_WithFullCapacity() {
        // given
        var flightService = new FlightService(null);
        var aircraft = new Aircraft();
        var flight = new Flight();

        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(50);
        aircraft.setEconomyCapacity(90);
        flight.setAircraft(aircraft);

        IntStream.range(0, 12).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Business.name());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium.name());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 90).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy.name());

            flight.getTickets().add(ticket);
        });

        // then
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Business));
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Premium));
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Economy));
    }

    @Test
    public void testIsTicketClassSeatsAvailable_WithAircraftNull() {
        // given
        var flightService = new FlightService(null);
        var flight = new Flight();

        // then
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Business));
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Premium));
        assertFalse(flightService.isTicketClassSeatsAvailable(flight, TicketClass.Economy));
    }

    @Test
    public void testIsSeatNumberValid() {
        // given
        var flightService = new FlightService(null);
        var aircraft = new Aircraft();
        var flight = new Flight();

        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(50);
        aircraft.setEconomyCapacity(90);
        flight.setAircraft(aircraft);

        // then
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Business, "1A"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Business, "4C"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Business, "12F"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "13E"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "32A"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "49F"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "51A"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "71B"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "82E"));

        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "13A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Premium, "51A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Economy, "91A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, ""));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "fdlkf3j4l35ljk"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "@A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "1$"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "1#1"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "1a"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "a1A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "0A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "A1"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "11111111A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "4F4F4F"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "4X"));
    }
}
