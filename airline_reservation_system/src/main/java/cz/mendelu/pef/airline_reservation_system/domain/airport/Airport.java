package cz.mendelu.pef.airline_reservation_system.domain.airport;

import cz.mendelu.pef.airline_reservation_system.domain.flight.Flight;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Table(name = "airport")
@Entity
@Data
@NoArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @Column(name = "country_code")
    @NotEmpty
    private String countryCode;

    @Column(name = "region_code")
    @NotEmpty
    private String regionCode;

    private String municipality;

    @Column(name = "gps_code")
    private String gpsCode;

    @NotNull
    @OneToMany(mappedBy = "airportDeparture")
    @EqualsAndHashCode.Exclude
    private Set<Flight> departureFlights = new HashSet<>();

    @NotNull
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "airportArrival")
    private Set<Flight> arrivalFlights = new HashSet<>();
}
