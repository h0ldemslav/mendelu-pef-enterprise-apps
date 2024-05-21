package cz.mendelu.pef.airline_reservation_system.domain.reports;

import cz.mendelu.pef.airline_reservation_system.utils.response.ObjectResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "", produces = "application/json")
    @Valid
    public ObjectResponse<Reports> getReports(
            @RequestBody @Valid ReportsRequest request
    ) {
        var startDate = request.getStartDate();
        var endDate = request.getEndDate();

        Reports reports = reportsService.getAllReports(
                startDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC),
                endDate.atTime(LocalTime.MIDNIGHT).atOffset(ZoneOffset.UTC)
        );

        return new ObjectResponse<>(reports);
    }
}
