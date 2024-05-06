package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TicketRequest {

    @NotEmpty
    private String number;

    @JsonProperty("class")
    @NotEmpty
    private String ticketClass;

    @JsonProperty("passenger_full_name")
    @NotEmpty
    private String passengerFullName;

    @JsonProperty("seat_number")
    private String seatNumber;

    private Long flightId;

    @NotNull
    private UUID customerId;

    public void toTicket(Ticket ticket, TicketClass ticketClass, Flight flight, Customer customer) {
        ticket.setNumber(this.number);
        ticket.setTicketClass(ticketClass.name());

        Double price = ticket.getPrice() == null ? flight.getFareTariff().getPriceByTicketClass(ticketClass) : ticket.getPrice();
        ticket.setPrice(price);

        Double discount = 0.0;

        if (ticket.getDiscount() != null) {
            discount = ticket.getDiscount();
            price -= discount;
        }

        ticket.setDiscount(discount);
        ticket.setPriceAfterDiscount(price);

        ticket.setSeatNumber(this.seatNumber);
        ticket.setPassengerFullName(this.passengerFullName);
        ticket.setDeparture(flight.getDeparture());
        ticket.setArrival(flight.getArrival());

        flight.getTickets().add(ticket);
        ticket.setFlight(flight);

        customer.getPurchasedTickets().add(ticket);
        ticket.setCustomer(customer);
    }
}
