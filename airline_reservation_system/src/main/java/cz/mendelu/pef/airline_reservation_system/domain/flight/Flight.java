package cz.mendelu.pef.airline_reservation_system.domain.flight;

import cz.mendelu.pef.airline_reservation_system.domain.aircraft.Aircraft;
import cz.mendelu.pef.airline_reservation_system.domain.airport.Airport;
import cz.mendelu.pef.airline_reservation_system.domain.fare_tariff.FareTariff;
import cz.mendelu.pef.airline_reservation_system.domain.ticket.Ticket;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "flight")
@Entity
@Data
@NoArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String number;

    @NotNull
    private OffsetDateTime departure;

    @NotNull
    private  OffsetDateTime arrival;

    @NotEmpty
    private String status;

    private Integer delay;

    @ManyToOne
    private Aircraft aircraft;

    @JoinColumn(name = "airport_departure_id")
    @NotNull
    @ManyToOne
    private Airport airportDeparture;

    @JoinColumn(name = "airport_arrival_id")
    @NotNull
    @ManyToOne
    private Airport airportArrival;

    @NotNull
    @OneToMany(mappedBy = "flight")
    private Set<Ticket> tickets = new HashSet<>();

    @JoinColumn(name = "fare_tariff_id")
    @NotNull
    @ManyToOne
    private FareTariff fareTariff;

    @PreRemove
    public void detachTickets() {
        tickets.forEach(t -> t.setFlight(null));
    }
}
