package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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
    private Set<Flight> flights = new HashSet<>();
}
