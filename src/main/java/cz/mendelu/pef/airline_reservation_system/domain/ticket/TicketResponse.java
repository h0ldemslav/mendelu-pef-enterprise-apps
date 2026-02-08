package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(example = "0000721866145")
    @NotEmpty
    private String number;

    @JsonProperty("class")
    @Schema(example = "Business")
    @NotNull
    private TicketClass ticketClass;

    @Schema(description = "Ticket price in dollars", example = "537.0")
    @NotNull
    @Positive
    private Double price;

    @Schema(description = "Ticket discount in dollars", example = "17.0")
    @NotNull
    @PositiveOrZero
    private Double discount;

    @JsonProperty("price_after_discount")
    @Schema(description = "Ticket price after subtracting discount", example = "520.0")
    @NotNull
    @Positive
    private Double priceAfterDiscount;

    @JsonProperty("seat_number")
    @Schema(description = "Valid seat number, digit and letter from [\"A\", \"B\", \"C\", \"D\", \"E\", \"F\"]", example = "1A")
    private String seatNumber;

    @JsonProperty("passenger_full_name")
    @Schema(example = "John Doe")
    @NotEmpty
    private String passengerFullName;

    @Schema(example = "2017-08-16T09:25:00Z")
    @NotNull
    private OffsetDateTime departure;

    @Schema(example = "2017-08-16T13:25:00Z")
    @NotNull
    private OffsetDateTime arrival;

    @JsonProperty("flight_id")
    @Schema(example = "9999")
    private Long flightId;

    @JsonProperty("customer_id")
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @NotNull
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
