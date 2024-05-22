package cz.mendelu.pef.airline_reservation_system.domain.reports;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class ReportsIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetReports() {
        ReportsRequest request = new ReportsRequest(
                LocalDate.of(2017, 7, 1),
                LocalDate.of(2017, 7, 31)
        );

        given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .get("/reports")
                .then()
                .statusCode(200)
                .body("content.ticket_sales", is(7576.0f))
                .body("content.ticket_class_distribution.Business", is(1))
                .body("content.ticket_class_distribution.Premium", is(1))
                .body("content.ticket_class_distribution.Economy", is(1))
                .body("content.top_5_popular_flight_ids_based_on_ticket_sales", contains(1, 2))
                .body("content.cancelled_and_delayed_flights.Delayed", is(0))
                .body("content.cancelled_and_delayed_flights.Cancelled", is(0))
                .body("content.passenger_load_factor_in_percentage", is(1.0416666f));
    }
}
