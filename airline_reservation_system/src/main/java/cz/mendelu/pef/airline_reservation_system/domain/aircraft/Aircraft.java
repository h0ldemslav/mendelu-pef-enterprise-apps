package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import cz.mendelu.pef.airline_reservation_system.utils.enums.TicketClass;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String code;

    @NotEmpty
    private String model;

    @Column(name = "business_capacity")
    @NotNull
    private Integer businessCapacity;

    @Column(name = "premium_capacity")
    @NotNull
    private Integer premiumCapacity;

    @Column(name = "economy_capacity")
    @NotNull
    private Integer economyCapacity;

    @NotNull
    @OneToMany(mappedBy = "aircraft")
    @EqualsAndHashCode.Exclude
    private Set<Flight> flights = new HashSet<>();

    @PreRemove
    public void detachFlights() {
        flights.forEach(fl -> fl.setAircraft(null));
    }

    public Integer getCapacityByTicketClass(TicketClass ticketClass) {
        return switch (ticketClass) {
            case Business -> this.businessCapacity;
            case Premium -> this.premiumCapacity;
            case Economy -> this.economyCapacity;
        };
    }

    public int getTotalCapacity() {
        if (this.businessCapacity == null || this.premiumCapacity == null || this.economyCapacity == null) {
            return 0;
        }

        return this.businessCapacity + this.premiumCapacity + this.economyCapacity;
    }

    public int getTotalNumberOfSeatRows(TicketClass ticketClass, int numberOfSeatsPerRow) {
        if (numberOfSeatsPerRow == 0) {
            return 0;
        }

        var capacity = this.getCapacityByTicketClass(ticketClass);

        return (int) Math.ceil((double) capacity / numberOfSeatsPerRow);
    }
}
