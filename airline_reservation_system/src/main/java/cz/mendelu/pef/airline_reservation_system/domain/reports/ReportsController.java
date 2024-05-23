package cz.mendelu.pef.airline_reservation_system.domain.reports;

import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("reports")
@Validated
public class ReportsController {

    private ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @Operation(summary = "Get reports")
    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ObjectResponse<Reports> getReports(
            @Parameter(
                    description = "Start date for the report range (inclusive)",
                    example = "2017-07-01"
            )
            @RequestParam @Valid LocalDate startDate,
            @Parameter(
                    description = "End date for the report range (exclusive)",
                    example = "2017-07-31"
            )
            @RequestParam @Valid LocalDate endDate
    ) {
        Reports reports = reportsService.getAllReports(
                startDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC),
                endDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC)
        );

        return new ObjectResponse<>(reports);
    }
}
