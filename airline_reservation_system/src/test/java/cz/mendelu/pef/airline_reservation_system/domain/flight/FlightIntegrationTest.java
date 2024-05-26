package cz.mendelu.pef.airline_reservation_system.domain.flight;

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

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class FlightIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetFlights() {
        given()
                .when()
                .get("/flights")
                .then()
                .statusCode(200)
                .body("count", is(3))
                .body("items[0].id", is(1))
                .body("items[0].number", is("AA0718"))
                .body("items[0].status", is("Scheduled"))
                .body("items[0].delay", is(nullValue()))
                .body("items[0].aircraft_id", is(1))
                .body("items[0].airport_departure_id", is(1))
                .body("items[0].airport_arrival_id", is(2))
                .body("items[0].fare_tariff_id", is(1))
                .body("items[1].id", is(2))
                .body("items[1].number", is("BB0531"))
                .body("items[1].status", is("Scheduled"))
                .body("items[1].delay", is(nullValue()))
                .body("items[1].aircraft_id", is(1))
                .body("items[1].airport_departure_id", is(2))
                .body("items[1].airport_arrival_id", is(1))
                .body("items[1].fare_tariff_id", is(2));
    }

    @Test
    public void testGetFlightById() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(1))
                .body("content.number", is("AA0718"))
                .body("content.status", is("Scheduled"))
                .body("content.delay", is(nullValue()))
                .body("content.aircraft_id", is(1))
                .body("content.airport_departure_id", is(1))
                .body("content.airport_arrival_id", is(2))
                .body("content.fare_tariff_id", is(1));
    }

    @Test
    public void testGetFlightById_NotFound() {
        final Long id = 999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetSeatNumbers() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}/seat_numbers")
                .then()
                .statusCode(200)
                .body("Business", not(containsInAnyOrder("1A")))
                .body("Premium", not(containsInAnyOrder("2A")));
    }

    @Test
    public void testGetSeatNumbers_FlightNotFound() {
        final Long id = 999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}/seat_numbers")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetFlightById_InvalidFlight_AircraftIsNull() {
        final FlightRequest request = new FlightRequest(
                "PG0335",
                OffsetDateTime.parse("2017-07-16T09:35:00Z"),
                OffsetDateTime.parse("2017-07-16T10:30:00Z"),
                "Scheduled",
                null,
                null,
                1L,
                2L,
                1L
        );

        var id = given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/flights")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}/seat_numbers")
                .then()
                .statusCode(422);
    }

    @Test
    public void testCreateFlight() {
        final FlightRequest request = new FlightRequest(
                "PG0335",
                OffsetDateTime.parse("2017-07-16T09:35:00Z"),
                OffsetDateTime.parse("2017-07-16T10:30:00Z"),
                "Scheduled",
                null,
                1L,
                1L,
                2L,
                1L
        );

        var id = given()
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .post("/flights")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id))
                .body("content.number", is("PG0335"))
                .body("content.status", is("Scheduled"))
                .body("content.delay", is(nullValue()))
                .body("content.aircraft_id", is(1))
                .body("content.airport_departure_id", is(1))
                .body("content.airport_arrival_id", is(2))
                .body("content.fare_tariff_id", is(1));
    }

    @Test
    public void testUpdateFlightById() {
        final Long id = 1L;
        final FlightRequest request = new FlightRequest(
                "PG0999",
                OffsetDateTime.parse("2017-07-16T10:35:00Z"),
                OffsetDateTime.parse("2017-07-16T11:30:00Z"),
                "Delayed",
                60,
                1L,
                1L,
                2L,
                1L
        );

        given()
                .pathParam("id", id)
                .body(request)
                .contentType(ContentType.JSON)
                .when()
                .put("/flights/{id}")
                .then()
                .statusCode(200);

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.intValue()))
                .body("content.number", is("PG0999"))
                .body("content.status", is("Delayed"))
                .body("content.departure", is("2017-07-16T10:35:00Z"))
                .body("content.arrival", is("2017-07-16T11:30:00Z"))
                .body("content.delay", is(60))
                .body("content.aircraft_id", is(1))
                .body("content.airport_departure_id", is(1))
                .body("content.airport_arrival_id", is(2))
                .body("content.fare_tariff_id", is(1));
    }

    @Test
    public void testCancelFlight() {
        final Long id = 1L;
        final double ticketDiscountPercentage = 5.0;

        final var businessPrice = given()
                .when()
                .get("/fare_tariffs/1")
                .then()
                .statusCode(200)
                .extract()
                .path("content.business_price");
        final var premiumPrice = given()
                .when()
                .get("/fare_tariffs/1")
                .then()
                .statusCode(200)
                .extract()
                .path("content.premium_price");

        given()
                .pathParam("id", id)
                .queryParam("ticket_discount_percentage", ticketDiscountPercentage)
                .when()
                .put("/flights/cancel/{id}")
                .then()
                .statusCode(200)
                .body("content.status", is("Cancelled"));

        // Verify that tickets got discount and price after discount is changed
        var premiumDiscount = (float) premiumPrice * (float) (ticketDiscountPercentage / 100);
        var premiumPriceAfterDiscount = (float) premiumPrice - premiumDiscount;

        given()
                .when()
                .get("/tickets/1")
                .then()
                .statusCode(200)
                .body("content.class", is("Premium"))
                .body("content.discount", is(premiumDiscount))
                .body("content.price_after_discount", is(premiumPriceAfterDiscount));

        var businessDiscount = (float) businessPrice * (float) (ticketDiscountPercentage / 100);
        var businessPriceAfterDiscount = (float) businessPrice - businessDiscount;

        given()
                .when()
                .get("/tickets/2")
                .then()
                .statusCode(200)
                .body("content.class", is("Business"))
                .body("content.discount", is(businessDiscount))
                .body("content.price_after_discount", is(businessPriceAfterDiscount));
    }

    @Test
    public void testCancelFlight_NotFound() {
        final Long id = 999L;
        final double ticketDiscountPercentage = 5.0;

        given()
                .pathParam("id", id)
                .queryParam("ticket_discount_percentage", ticketDiscountPercentage)
                .when()
                .put("/flights/cancel/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteFlight() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .delete("/flights/{id}")
                .then()
                .statusCode(204);

        given()
                .pathParam("id", id)
                .when()
                .get("/flights/{id}")
                .then()
                .statusCode(404);
    }
}
