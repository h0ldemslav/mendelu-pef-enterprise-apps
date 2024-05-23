package cz.mendelu.pef.airline_reservation_system.domain.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Reports {

    @JsonProperty("ticket_sales")
    @Schema(example = "1000000.57")
    Double ticketSales;

    @JsonProperty("ticket_class_distribution")
    @Schema(example = "{ \"Economy\":1842,\"Premium\": 190, \"Business\": 42 }")
    Map<String, Long> ticketClassDistribution;

    @JsonProperty("top_5_popular_flight_ids_based_on_ticket_sales")
    @Schema(example = "[2551, 1039, 190, 9, 1505]")
    List<Long> top5PopularFlightIdsBasedOnTicketSales;

    @JsonProperty("cancelled_and_delayed_flights")
    @Schema(example = "{ \"Delayed\": 7, \"Cancelled\": 1 }")
    Map<String, Long> cancelledAndDelayedFlights;

    @JsonProperty("passenger_load_factor_in_percentage")
    @Schema(example = "47.597552795840535")
    Double passengerLoadFactorInPercentage;
}
