package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class TicketResponse {

    private Long id;

    @NotEmpty
    private String number;

    @JsonProperty("class")
    @NotEmpty
    private TicketClass ticketClass;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @PositiveOrZero
    private Double discount;

    @JsonProperty("price_after_discount")
    @NotNull
    @Positive
    private Double priceAfterDiscount;

    @JsonProperty("seat_number")
    private String seatNumber;

    @JsonProperty("passenger_full_name")
    @NotEmpty
    private String passengerFullName;

    @NotNull
    private OffsetDateTime departure;

    @NotNull
    private OffsetDateTime arrival;

    @JsonProperty("flight_id")
    private Long flightId;

    @NotNull
    @JsonProperty("customer_id")
    private UUID customerId;

    TicketResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.number = ticket.getNumber();
        this.ticketClass = ticket.getTicketClass();
        this.price = ticket.getPrice();
        this.discount = ticket.getDiscount();
        this.priceAfterDiscount = ticket.getPriceAfterDiscount();
        this.seatNumber = ticket.getSeatNumber();
        this.passengerFullName = ticket.getPassengerFullName();
        this.departure = ticket.getDeparture();
        this.arrival = ticket.getArrival();
        this.customerId = ticket.getCustomer().getId();

        if (ticket.getFlight() != null) {
            this.flightId = ticket.getFlight().getId();
        }
    }
}
