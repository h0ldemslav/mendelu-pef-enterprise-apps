package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.customer.CustomerService;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.domain.flight.FlightService;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.BadRequestException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NoAvailableSeatException;
import cz.mendelu.pef.airline_reservation_system.utils.exceptions.NotFoundException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketRequest {

    @NotEmpty
    private String number;

    @JsonProperty("class")
    @NotEmpty
    private String ticketClass;

    private Long flightId;

    @NotNull
    private UUID customerId;

    public void toTicket(Ticket ticket, CustomerService customerService, FlightService flightService) {
        Customer customer = customerService
                .getCustomerById(this.customerId)
                .orElseThrow(NotFoundException::new);
        Flight flight = flightService
                .getFlightById(this.flightId)
                .orElseThrow(NotFoundException::new);
        TicketClass ticketClassFromString = TicketClass
                .getTicketClassByString(this.ticketClass)
                .orElseThrow(BadRequestException::new);

        if (!flightService.isTicketClassSeatsAvailable(flight, ticketClassFromString)) {
            throw new NoAvailableSeatException();
        }

        customer.getPurchasedTickets().add(ticket);
        ticket.setCustomer(customer);
        ticket.setPassengerFullName(customer.getFullName());

        flight.getTickets().add(ticket);
        ticket.setFlight(flight);

        ticket.setSeatNumber(flightService.getSeatNumber(flight, ticketClassFromString));
        ticket.setDeparture(flight.getDeparture());
        ticket.setArrival(flight.getArrival());
        ticket.setNumber(this.number);
        ticket.setTicketClass(ticketClassFromString.name());
        Double price = flight
                .getFareTariff()
                .getPriceByTicketClass(ticketClassFromString);
        ticket.setPrice(price);
        ticket.setDiscount(0.0);
        ticket.setPriceAfterDiscount(price);
    }
}
