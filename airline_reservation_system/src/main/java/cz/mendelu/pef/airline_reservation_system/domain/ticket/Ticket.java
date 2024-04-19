package cz.mendelu.pef.airline_reservation_system.domain.ticket;

import cz.mendelu.pef.airline_reservation_system.domain.customer.Customer;
import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Table(name = "ticket")
@Entity
@Data
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String number = "";

    @Column(name = "class")
    @NotEmpty
    private String ticketClass;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Double discount;

    @Column(name = "price_after_discount")
    @NotNull
    @Positive
    private Double priceAfterDiscount;

    @Column(name = "seat_number")
    @NotEmpty
    private String seatNumber;

    @Column(name = "passenger_full_name")
    @NotEmpty
    private String passengerFullName;

    @NotNull
    private OffsetDateTime departure;

    @NotNull
    private OffsetDateTime arrival;

    @NotNull
    @ManyToOne
    private Flight flight;

    @NotNull
    @ManyToOne
    private Customer customer;
}
