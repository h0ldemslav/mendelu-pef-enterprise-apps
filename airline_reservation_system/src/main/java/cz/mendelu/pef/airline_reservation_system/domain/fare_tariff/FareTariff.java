package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

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
@Table(name = "fare_tariff")
@NoArgsConstructor
public class FareTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String code;

    @Column(name = "business_price")
    @NotNull
    private Double businessPrice;

    @Column(name = "premium_price")
    @NotNull
    private Double premiumPrice;

    @Column(name = "economy_price")
    @NotNull
    private Double economyPrice;

    @NotNull
    @OneToMany(mappedBy = "fareTariff")
    private Set<Flight> flights = new HashSet<>();
}
