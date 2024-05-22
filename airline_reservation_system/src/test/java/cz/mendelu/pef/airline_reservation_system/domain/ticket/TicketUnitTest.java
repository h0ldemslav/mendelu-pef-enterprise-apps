package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.airport.Airport;
import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidFlightException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.InvalidTransferInformationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TicketUnitTest {

    @Test
    public void testAssignSeatNumber() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Flight flight = getFlightForTesting();
        Customer customer = new Customer();
        customer.setCredit(10000.0);

        Ticket ticket = new Ticket();
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setPrice(flight.getFareTariff().getEconomyPrice());
        ticket.setPriceAfterDiscount(flight.getFareTariff().getEconomyPrice());
        ticket.setCustomer(customer);
        ticket.setFlight(flight);

        var fareTariffPrice = flight.getFareTariff().getPriceByTicketClass(ticket.getTicketClass());

        // when
        var customerCreditBeforePurchase = customer.getCredit();
        ticketService.assignSeatNumber(flight, ticket);
        var customerCreditAfterPurchase = customer.getCredit();

        // then
        assertThat(ticket.getSeatNumber(), is("7A"));
        assertThat(ticket.getPrice(), is(fareTariffPrice));
        assertThat(ticket.getPriceAfterDiscount(), is(fareTariffPrice));
        assertThat(customerCreditAfterPurchase, is(customerCreditBeforePurchase - fareTariffPrice));
    }

    @Test
    public void testAssignSeatNumber_WithCustomSeat() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Flight flight = getFlightForTesting();
        Customer customer = new Customer();
        customer.setCredit(10000.0);

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setPrice(flight.getFareTariff().getEconomyPrice());
        ticket.setPriceAfterDiscount(flight.getFareTariff().getEconomyPrice());
        ticket.setCustomer(customer);
        ticket.setSeatNumber("7F");

        var fareTariffPrice = flight.getFareTariff().getPriceByTicketClass(ticket.getTicketClass());
        var feeForSeatNumber = ticketService.getTicketExtraPriceForCustomSeat(ticket)
                .orElseThrow(InvalidFlightException::new);

        // when
        var customerCreditBeforePurchase = customer.getCredit();
        ticketService.assignSeatNumber(flight, ticket);
        var customerCreditAfterPurchase = customer.getCredit();

        // then
        assertThat(ticket.getSeatNumber(), is("7F"));
        assertThat(ticket.getPrice(), is(fareTariffPrice + feeForSeatNumber));
        assertThat(ticket.getPriceAfterDiscount(), is(fareTariffPrice + feeForSeatNumber));
        assertThat(customerCreditAfterPurchase, is(customerCreditBeforePurchase - fareTariffPrice - feeForSeatNumber));
    }

    @Test
    public void testAssignSeatNumber_InvalidFlightException() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Customer customer = new Customer();
        customer.setCredit(10000.0);

        Ticket ticket = new Ticket();
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setCustomer(customer);

        // when
        var customerCreditBeforePurchase = customer.getCredit();

        assertThrows(
                InvalidFlightException.class,
                () -> ticketService.assignSeatNumber(null, ticket)
        );

        var customerCreditAfterPurchase = customer.getCredit();

        // then
        assertThat(ticket.getSeatNumber(), is(nullValue()));
        assertThat(ticket.getPrice(), is(nullValue()));
        assertThat(ticket.getPriceAfterDiscount(), is(nullValue()));
        assertThat(customerCreditBeforePurchase, is(customerCreditAfterPurchase));
    }

    @Test
    public void testIsTicketClassUpgradeValid() {
        // given
        TicketService ticketService = new TicketService(null, null, null);

        var oldTicketClass1 = TicketClass.Economy;
        var newTicketClass1 = TicketClass.Premium;

        var oldTicketClass2 = TicketClass.Business;
        var newTicketClass2 = TicketClass.Economy;

        var oldTicketClass3 = TicketClass.Premium;
        var newTicketClass3 = TicketClass.Premium;

        var oldTicketClass4 = TicketClass.Premium;
        var newTicketClass4 = TicketClass.Economy;

        var oldTicketClass5 = TicketClass.Premium;
        var newTicketClass5 = TicketClass.Business;

        // when
        boolean result1 = ticketService.isTicketClassUpgradeValid(newTicketClass1, oldTicketClass1);
        boolean result2 = ticketService.isTicketClassUpgradeValid(newTicketClass2, oldTicketClass2);
        boolean result3 = ticketService.isTicketClassUpgradeValid(newTicketClass3, oldTicketClass3);
        boolean result4 = ticketService.isTicketClassUpgradeValid(newTicketClass4, oldTicketClass4);
        boolean result5 = ticketService.isTicketClassUpgradeValid(newTicketClass5, oldTicketClass5);

        // then
        assertThat(result1, is(true));
        assertThat(result2, is(false));
        assertThat(result3, is(false));
        assertThat(result4, is(false));
        assertThat(result5, is(true));
    }

    @Test
    public void testUpgradeTicketClass() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Flight flight = getFlightForTesting();
        Customer customer = new Customer();
        customer.setCredit(10000.0);

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setPrice(flight.getFareTariff().getEconomyPrice());
        ticket.setPriceAfterDiscount(flight.getFareTariff().getEconomyPrice());
        ticket.setCustomer(customer);

        var premiumPrice = flight.getFareTariff().getPremiumPrice();

        // then
        var expectedCustomerCredit = customer.getCredit() - (premiumPrice - ticket.getPrice());
        ticketService.upgradeTicketClass(ticket, TicketClass.Premium);
        var customerCreditAfterPurchase = customer.getCredit();

        // when
        assertThat(ticket.getTicketClass().name(), is(TicketClass.Premium.name()));
        assertThat(ticket.getPrice(), is(premiumPrice));
        assertThat(ticket.getPriceAfterDiscount(), is(premiumPrice));
        assertThat(customerCreditAfterPurchase, is(expectedCustomerCredit));
    }

    @Test
    public void testTransferTicketToOtherFlight() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Flight flight = getFlightForTesting();
        Customer customer = new Customer();
        customer.setCredit(10000.0);

        FareTariff fareTariff = new FareTariff();
        fareTariff.setEconomyPrice(977.0);
        fareTariff.setPremiumPrice(11472.0);
        fareTariff.setBusinessPrice(21890.0);

        Flight newFlight = new Flight();
        newFlight.setDeparture(OffsetDateTime.parse("2017-07-31T09:35:00+01:00"));
        newFlight.setArrival(OffsetDateTime.parse("2017-07-31T12:35:00+01:00"));
        newFlight.setFareTariff(fareTariff);
        newFlight.setAircraft(flight.getAircraft());

        Ticket ticket = new Ticket();
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setPrice(flight.getFareTariff().getEconomyPrice());
        ticket.setPriceAfterDiscount(flight.getFareTariff().getEconomyPrice());
        ticket.setCustomer(customer);
        ticket.setFlight(flight);

        // when
        ticketService.transferTicketToOtherFlight(ticket, newFlight);

        // then
        assertThat(ticket.getFlight(), is(newFlight));
        assertThat(ticket.getDeparture(), is(newFlight.getDeparture()));
        assertThat(ticket.getArrival(), is(newFlight.getArrival()));
    }

    @Test
    public void testTransferTicketToOtherFlight_SameFlight() {
        // given
        FlightService flightService = new FlightService(null);
        CustomerService customerService = new CustomerService(null);
        TicketService ticketService = new TicketService(null, flightService, customerService);

        Flight flight = getFlightForTesting();
        Customer customer = new Customer();
        customer.setCredit(10000.0);

        Ticket ticket = new Ticket();
        ticket.setTicketClass(TicketClass.Economy);
        ticket.setPrice(flight.getFareTariff().getEconomyPrice());
        ticket.setPriceAfterDiscount(flight.getFareTariff().getEconomyPrice());
        ticket.setCustomer(customer);
        ticket.setFlight(flight);
        ticket.setDeparture(flight.getDeparture());
        ticket.setArrival(flight.getArrival());

        // when
        assertThrows(
                InvalidTransferInformationException.class,
                () -> ticketService.transferTicketToOtherFlight(ticket, flight)
        );

        // then
        assertThat(ticket.getFlight(), is(flight));
        assertThat(ticket.getDeparture(), is(flight.getDeparture()));
        assertThat(ticket.getArrival(), is(flight.getArrival()));
    }

    private static Flight getFlightForTesting() {
        Aircraft aircraft = new Aircraft();
        aircraft.setEconomyCapacity(50);
        aircraft.setBusinessCapacity(6);
        aircraft.setPremiumCapacity(30);

        Airport airport1 = new Airport();
        airport1.setLatitude(31.255053);
        airport1.setLongitude(-81.466932);

        Airport airport2 = new Airport();
        airport2.setLatitude(47.491676);
        airport2.setLongitude(21.609334);

        FareTariff fareTariff = new FareTariff();
        fareTariff.setEconomyPrice(877.0);
        fareTariff.setPremiumPrice(10472.0);
        fareTariff.setBusinessPrice(20890.0);

        Flight flight = new Flight();
        flight.setId(1L);
        flight.setNumber("AA0718");
        flight.setDeparture(OffsetDateTime.parse("2017-07-16T09:35:00+01:00"));
        flight.setArrival(OffsetDateTime.parse("2017-07-16T12:35:00+01:00"));
        flight.setStatus("Scheduled");
        flight.setAircraft(aircraft);
        flight.setAirportDeparture(airport1);
        flight.setAirportArrival(airport2);
        flight.setFareTariff(fareTariff);

        Set<Ticket> flightTickets = flight.getTickets();

        List<String> seatNumberLetters = List.of("A", "B", "C", "D", "E", "F");
        IntStream.range(0, 6).forEach(i -> {
            var ticket = new Ticket();
            ticket.setId(Integer.toUnsignedLong(i + 1));
            ticket.setTicketClass(TicketClass.Business);
            ticket.setSeatNumber(1 + seatNumberLetters.get(i));
            ticket.setPrice(fareTariff.getBusinessPrice());
            ticket.setDiscount(0.0);
            ticket.setPriceAfterDiscount(fareTariff.getBusinessPrice());

            flightTickets.add(ticket);
        });

        airport1.getDepartureFlights().add(flight);
        airport2.getArrivalFlights().add(flight);

        return flight;
    }
}
