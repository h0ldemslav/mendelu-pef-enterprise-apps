package cz.mendelu.pef.airline_reservation_system.domain.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Reports {

    @JsonProperty("ticket_sales")
    Double ticketSales;

    @JsonProperty("ticket_class_distribution")
    Map<String, Long> ticketClassDistribution;

    @JsonProperty("top_5_popular_flights_base_on_ticket_sales")
    List<Long> top5PopularFlightsBasedOnTicketSales;

    @JsonProperty("passenger_load_factor_in_percentage")
    Long passengerLoadFactorInPercentage;

    @JsonProperty("cancelled_and_delayed_flights")
    Map<String, Long> cancelledAndDelayedFlights;
}
