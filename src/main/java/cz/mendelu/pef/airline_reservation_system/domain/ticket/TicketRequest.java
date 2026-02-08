package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TicketRequest {

    @Schema(example = "0000721866145")
    @NotEmpty
    private String number;

    @JsonProperty("class")
    @Schema(example = "Business")
    @NotNull
    private TicketClass ticketClass;

    @JsonProperty("passenger_full_name")
    @Schema(example = "John Doe")
    @NotEmpty
    private String passengerFullName;

    @JsonProperty("seat_number")
    @Schema(description = "Valid seat number, integer and letter from [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\"]", example = "1A")
    private String seatNumber;

    @Schema(example = "9999")
    private Long flightId;

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull
    private UUID customerId;

    public void toTicket(Ticket ticket, Flight flight, Customer customer) {
        ticket.setNumber(this.number);
        ticket.setTicketClass(this.ticketClass);

        // Ticket price can include a fee for custom seat,
        // so that is why `flight.getFareTariff().getPriceByTicketClass(ticketClass)` is not by default
        // Otherwise it would override the price with fee (if so)
        Double price = ticket.getPrice() == null
                ? flight.getFareTariff().getPriceByTicketClass(this.ticketClass)
                : ticket.getPrice();
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
