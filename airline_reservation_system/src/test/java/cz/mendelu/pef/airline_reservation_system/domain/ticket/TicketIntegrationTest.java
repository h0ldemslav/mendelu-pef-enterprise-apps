package cz.mendelu.pef.airline_reservation_system.domain.ticket;

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

import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static java.lang.Float.parseFloat;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class TicketIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetTickets() {
        given()
                .when()
                .get("/tickets")
                .then()
                .statusCode(200)
                .body("count", is(3))
                .body("items[0].number", is("0008386672215"))
                .body("items[0].class", is("Premium"))
                .body("items[0].price", is(1326.f))
                .body("items[0].discount", is(0.0f))
                .body("items[0].price_after_discount", is(1326.0f))
                .body("items[0].seat_number", is("2A"))
                .body("items[0].passenger_full_name", is("Marry Smith"))
                // TODO: fix problem with time in postgres
                // .body("content.departure", is("2017-07-16T09:35:00Z"))
                // .body("content.arrival", is("2017-07-16T10:30:00Z"))
                .body("items[0].flight_id", is(1))
                .body("items[0].customer_id", is("35645ea7-2b38-430d-aab8-f72302cdc2c8"));
    }

    @Test
    public void testGetTestById() {
        final Long id = 2L;

        given()
                .pathParam("id", id)
                .when()
                .get("/tickets/{id}")
                .then()
                .statusCode(200)
                .body("content.number", is("0003099269080"))
                .body("content.class", is("Business"))
                .body("content.price", is(5513.0f))
                .body("content.discount", is(0.0f))
                .body("content.price_after_discount", is(5513.0f))
                .body("content.seat_number", is("1A"))
                .body("content.passenger_full_name", is("Ben Rope"))
                // .body("content.departure", is("2017-07-16T09:35:00Z"))
                // .body("content.arrival", is("2017-07-16T10:30:00Z"))
                .body("content.flight_id", is(1))
                .body("content.customer_id", is("0931192f-35a2-461b-bc85-133290c63c67"));
    }

    @Test
    public void testGetTestById_NotFound() {
        final Long id = 999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/tickets/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateTicket() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "John Smith",
                null,
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        var id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/tickets/{id}")
                .then()
                .statusCode(200)
                .body("content.number", is("0004099267192"))
                .body("content.class", is("Premium"))
                .body("content.price", is(1326.f))
                .body("content.discount", is(0.0f))
                .body("content.price_after_discount", is(1326.f))
                .body("content.seat_number", is("2B"))
                .body("content.passenger_full_name", is("John Smith"))
                // .body("content.departure", is("2017-07-16T09:35:00Z"))
                // .body("content.arrival", is("2017-07-16T10:30:00Z"))
                .body("content.flight_id", is(1))
                .body("content.customer_id", is("35645ea7-2b38-430d-aab8-f72302cdc2c8"));
    }

    @Test
    public void testCreateTicket_CustomerNotFound() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "John Smith",
                null,
                1L,
                UUID.fromString("f6f729db-6886-4f21-9cf4-667b47adc490")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateTicket_FlightNotFound() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "John Smith",
                null,
                999L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateTicket_WithCustomSeat() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "Bob Smith",
                "2F",
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        var id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        // Custom seat costs additional fee, 10% of ticket price
        final float expectedTicketPrice = 1326.f + (1326.f * 0.1f);

        given()
                .pathParam("id", id)
                .when()
                .get("/tickets/{id}")
                .then()
                .statusCode(200)
                .body("content.number", is("0004099267192"))
                .body("content.class", is("Premium"))
                .body("content.price", is(expectedTicketPrice))
                .body("content.discount", is(0.0f))
                .body("content.price_after_discount", is(expectedTicketPrice))
                .body("content.seat_number", is("2F"))
                .body("content.passenger_full_name", is("Bob Smith"))
                // .body("content.departure", is("2017-07-16T09:35:00Z"))
                // .body("content.arrival", is("2017-07-16T10:30:00Z"))
                .body("content.flight_id", is(1))
                .body("content.customer_id", is("35645ea7-2b38-430d-aab8-f72302cdc2c8"));
    }

    @Test
    public void testCreateTicket_NoAvailableSeat() {
        final TicketRequest request1 = new TicketRequest(
                "0007799652613",
                "Business",
                "Bob Rope",
                null,
                1L,
                UUID.fromString("0931192f-35a2-461b-bc85-133290c63c67")
        );
        final TicketRequest request2 = new TicketRequest(
                "0001241598283",
                "Business",
                "Casey Rope",
                null,
                1L,
                UUID.fromString("0931192f-35a2-461b-bc85-133290c63c67")
        );
        final TicketRequest request3 = new TicketRequest(
                "0004099267192",
                "Business",
                "John Rope",
                null,
                1L,
                UUID.fromString("0931192f-35a2-461b-bc85-133290c63c67")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request1)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(request2)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(request3)
                .when()
                .post("/tickets")
                .then()
                .statusCode(422);
    }

    @Test
    public void testCreateTicket_InvalidCustomSeat() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "Helen Smith",
                // Invalid custom seat number
                "20X",
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(422);
    }

    @Test
    public void testCreateTicket_SeatIsOccupied() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "Helen Smith",
                // Valid custom seat number, but it's already occupied by other passenger
                "2A",
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(422);
    }

    @Test
    public void testCreateTicket_NotEnoughCustomerCredit() {
        final TicketRequest request = new TicketRequest(
                "0004099267192",
                "Premium",
                "Helen Doe",
                null,
                2L,
                UUID.fromString("da8d38a8-28a7-4740-8096-f0197ebabd5c")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(422);
    }

    @Test
    public void testChangeSeatNumber() {
        final Long ticketId = 1L;
        final String newSeatNumber = "2F";
        final float priceForSeatChange = (1326.0f * 0.1f);
        final float expectedTicketPrice = 1326.0f + priceForSeatChange;

        given()
                .pathParam("id", ticketId)
                .queryParam("seatNumber", newSeatNumber)
                .when()
                .put("/tickets/{id}/change_seat_number")
                .then()
                .statusCode(200)
                .body("content.number", is("0008386672215"))
                .body("content.class", is("Premium"))
                .body("content.price", is(expectedTicketPrice))
                .body("content.discount", is(0.0f))
                .body("content.price_after_discount", is(expectedTicketPrice))
                .body("content.seat_number", is("2F"))
                .body("content.passenger_full_name", is("Marry Smith"))
                // .body("content.departure", is("2017-07-16T09:35:00Z"))
                // .body("content.arrival", is("2017-07-16T10:30:00Z"))
                .body("content.flight_id", is(1))
                .body("content.customer_id", is("35645ea7-2b38-430d-aab8-f72302cdc2c8"));

        final float expectedCustomerCredit = 19999.81f - priceForSeatChange;

        given()
                .pathParam("id", "35645ea7-2b38-430d-aab8-f72302cdc2c8")
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("content.credit", is(expectedCustomerCredit));
    }

    @Test
    public void testChangeSeatNumber_TicketNotFound() {
        final Long ticketId = 999L;
        final String newSeatNumber = "2F";

        given()
                .pathParam("id", ticketId)
                .queryParam("seatNumber", newSeatNumber)
                .when()
                .put("/tickets/{id}/change_seat_number")
                .then()
                .statusCode(404);
    }

    @Test
    public void testChangeSeatNumber_InvalidSeatNumber() {
        final Long ticketId = 1L;
        final String newSeatNumber = "2X";

        given()
                .pathParam("id", ticketId)
                .queryParam("seatNumber", newSeatNumber)
                .when()
                .put("/tickets/{id}/change_seat_number")
                .then()
                .statusCode(422);
    }

    @Test
    public void testChangeSeatNumber_SeatIsOccupied() {
        final TicketRequest request = new TicketRequest(
                "0008024034546",
                "Premium",
                "Jain Smith",
                "2F",
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201);

        final Long ticketId = 1L;
        final String newSeatNumber = "2F";

        given()
                .pathParam("id", ticketId)
                .queryParam("seatNumber", newSeatNumber)
                .when()
                .put("/tickets/{id}/change_seat_number")
                .then()
                .statusCode(422);
    }

    @Test
    public void testChangeSeatNumber_NotEnoughCustomerCredit() {
        // Decreasing customer credit, so there is not enough credit to change a seat in a next request
        IntStream.range(0, 15).forEach(i -> {
            final TicketRequest request = new TicketRequest(
                    "0008024034546",
                    "Premium",
                    "Jain Smith",
                    null,
                    1L,
                    UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/tickets")
                    .then()
                    .statusCode(201);
        });

        final Long ticketId = 1L;
        final String newSeatNumber = "4F";

        given()
                .pathParam("id", ticketId)
                .queryParam("seatNumber", newSeatNumber)
                .when()
                .put("/tickets/{id}/change_seat_number")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpgradeTicketClass() {
        final Long ticketId = 1L;
        final String newTicketClass = "Business";
        final float expectedPriceForUpgrade = 5513.0f - 1326.0f;
        // This is needed, because otherwise the calculation has one more digit in part after decimal point
        final float expectedCustomerCredit = parseFloat(String.format(
                Locale.CANADA,
                "%.2f",
                19999.81 - (double) expectedPriceForUpgrade
                )
        );

        var customerId = given()
                .pathParam("id", ticketId)
                .queryParam("newTicketClass", newTicketClass)
                .when()
                .put("/tickets/{id}/upgrade_ticket_class")
                .then()
                .statusCode(200)
                .body("content.class", is("Business"))
                .body("content.price", is(5513.0f))
                .body("content.price_after_discount", is(5513.0f))
                .extract()
                .path("content.customer_id");

        given()
                .pathParam("id", customerId)
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("content.credit", is(expectedCustomerCredit));
    }

    @Test
    public void testUpgradeTicketClass_TicketNotFound() {
        final Long ticketId = 999L;
        final String newTicketClass = "Business";

        given()
                .pathParam("id", ticketId)
                .queryParam("newTicketClass", newTicketClass)
                .when()
                .put("/tickets/{id}/upgrade_ticket_class")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpgradeTicketClass_InvalidTicketClass() {
        final Long ticketId = 1L;
        // It's not possible to downgrade ticket
        // So here `Economy` considered as invalid ticket class
        final String newTicketClass = "Economy";

        given()
                .pathParam("id", ticketId)
                .queryParam("newTicketClass", newTicketClass)
                .when()
                .put("/tickets/{id}/upgrade_ticket_class")
                .then()
                .statusCode(409);
    }

    @Test
    public void testUpgradeTicketClass_NoAvailableSeat() {
        final TicketRequest request1 = new TicketRequest(
                "0000456021522",
                "Business",
                "Jain Smith",
                null,
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );
        final TicketRequest request2 = new TicketRequest(
                "0002920841694",
                "Business",
                "Rose Smith",
                null,
                1L,
                UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8")
        );

        given()
                .contentType(ContentType.JSON)
                .body(request1)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)

                .body(request2)
                .when()
                .post("/tickets")
                .then()
                .statusCode(201);

        final Long ticketId = 1L;
        final String newTicketClass = "Business";

        given()
                .pathParam("id", ticketId)
                .queryParam("newTicketClass", newTicketClass)
                .when()
                .put("/tickets/{id}/upgrade_ticket_class")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpgradeTicketClass_NotEnoughCustomerCredit() {
        final Long ticketId = 3L;
        final String newTicketClass = "Business";

        given()
                .pathParam("id", ticketId)
                .queryParam("newTicketClass", newTicketClass)
                .when()
                .put("/tickets/{id}/upgrade_ticket_class")
                .then()
                .statusCode(422);
    }

    @Test
    public void testDeleteTicketById() {
        final Long ticketId = 1L;

        given()
                .pathParam("id", ticketId)
                .when()
                .delete("/tickets/{id}")
                .then()
                .statusCode(204);

        given()
                .pathParam("id", ticketId)
                .when()
                .get("/tickets/{id}")
                .then()
                .statusCode(404);
    }
}
