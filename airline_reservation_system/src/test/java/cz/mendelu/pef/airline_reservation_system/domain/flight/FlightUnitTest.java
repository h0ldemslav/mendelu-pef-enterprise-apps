package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
            ticket.setTicketClass(TicketClass.Business);
            ticket.setSeatNumber(flightService.getSeatNumber(flight, TicketClass.Business).orElseThrow());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 6).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium);
            ticket.setSeatNumber(flightService.getSeatNumber(flight, TicketClass.Premium).orElseThrow());

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy);
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
            ticket.setTicketClass(TicketClass.Business);

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 10).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium);

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy);

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
            ticket.setTicketClass(TicketClass.Business);

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 50).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Premium);

            flight.getTickets().add(ticket);
        });
        IntStream.range(0, 90).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i));
            ticket.setTicketClass(TicketClass.Economy);

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
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Business, "1D"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Business, "2F"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "4E"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "5A"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Premium, "6F"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "12A"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "15B"));
        assertTrue(flightService.isSeatNumberValid(flight, TicketClass.Economy, "22E"));

        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Business, "3A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Premium, "12A"));
        assertFalse(flightService.isSeatNumberValid(flight, TicketClass.Economy, "29A"));
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

    @Test
    public void testCancelFlight() {
        // given
        var flightRepository = mock(FlightRepository.class);
        var flightService = new FlightService(flightRepository);

        var aircraft = new Aircraft();
        aircraft.setBusinessCapacity(12);
        aircraft.setPremiumCapacity(50);
        aircraft.setEconomyCapacity(90);

        var fareTariff = new FareTariff();
        fareTariff.setBusinessPrice(5513.0);
        fareTariff.setPremiumPrice(1326.0);
        fareTariff.setEconomyPrice(624.0);

        var flight = new Flight();
        flight.setId(1L);
        flight.setAircraft(aircraft);
        flight.setFareTariff(fareTariff);

        var ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setTicketClass(TicketClass.Business);
        ticket1.setPrice(fareTariff.getPremiumPrice());
        ticket1.setDiscount(0.0);
        ticket1.setPriceAfterDiscount(ticket1.getPrice());

        var ticket2 = new Ticket();
        ticket2.setId(2L);
        ticket2.setTicketClass(TicketClass.Premium);
        ticket2.setPrice(fareTariff.getBusinessPrice());
        ticket2.setDiscount(0.0);
        ticket2.setPriceAfterDiscount(ticket2.getPrice());

        Set<Ticket> tickets = Set.of(ticket1, ticket2);
        flight.setTickets(tickets);

        // when
        var ticketDiscountPercentage = 10.0;

        flightService.cancelFlight(flight, ticketDiscountPercentage);
        flightService.updateFlight(flight.getId(), flight);

        // then
        verify(flightRepository).save(flight);

        assertThat(flight.getStatus(), is("Cancelled"));

        var ticket1NewDiscount = ticket1.getPrice() * (ticketDiscountPercentage / 100);
        var ticket1NewPriceAfterDiscount = ticket1.getPrice() - ticket1NewDiscount;

        assertEquals(ticket1.getDiscount(), ticket1NewDiscount);
        assertEquals(ticket1.getPriceAfterDiscount(), ticket1NewPriceAfterDiscount);

        var ticket2NewDiscount = ticket2.getPrice() * (ticketDiscountPercentage / 100);
        var ticket2NewPriceAfterDiscount = ticket2.getPrice() - ticket2NewDiscount;

        assertEquals(ticket2.getDiscount(), ticket2NewDiscount);
        assertEquals(ticket2.getPriceAfterDiscount(), ticket2NewPriceAfterDiscount);
    }
}
