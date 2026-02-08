package cz.mendelu.pef.airline_reservation_system.domain.customer;

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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class CustomerIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetCustomers() {
        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("count", is(3))
                .body("items[0].id", is("35645ea7-2b38-430d-aab8-f72302cdc2c8"))
                .body("items[0].first_name", is("Marry"))
                .body("items[0].last_name", is("Smith"))
                .body("items[0].credit", is(19999.81f))
                .body("items[0].phone", is("+351 326 363 8324"))
                .body("items[0].email", is("marryS10@reverbnation.com"))
                .body("items[0].password", is("$2a$04$bWi8kKs0kLQi9ao03TrWeemIniME785I3uMISQRfibYwyhl55SJMi"));
    }

    @Test
    public void testGetCustomerById() {
        final UUID id = UUID.fromString("0931192f-35a2-461b-bc85-133290c63c67");

        given()
                .pathParam("id", id.toString())
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.toString()))
                .body("content.first_name", is("Ben"))
                .body("content.last_name", is("Rope"))
                .body("content.credit", is(30001.81f))
                .body("content.phone", is("+42 326 363 8324"))
                .body("content.email", is("benRO11P@reverbnation.com"))
                .body("content.password", is("$2a$04$Pu.MMm4WemMDrb5ckNdMF.GzwQ3PVaMCpavhRccDzN9NANFsnKOXu"));
    }

    @Test
    public void testGetCustomerById_NotFound() {
        final UUID id = UUID.fromString("0ffd2580-5e25-43db-8eed-a1a3d9e72b6d");

        given()
                .pathParam("id", id.toString())
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetFlightRecommendationsForCustomerById() {
        final UUID id = UUID.fromString("0931192f-35a2-461b-bc85-133290c63c67");

        given()
                .pathParam("id", id)
                .when()
                .get("/customers/{id}/recommendations")
                .then()
                .statusCode(200)
                .body("items.size()", is(2))
                .body("items[0].id", is(1))
                .body("items[0].status", is("Scheduled"))
                .body("items[0].airport_arrival_id", is(2))
                .body("items[1].id", is(3))
                .body("items[1].status", is("Scheduled"))
                .body("items[1].airport_arrival_id", is(2));
    }

    @Test
    public void testGetFlightRecommendationsForCustomerById_NotFound() {
        final UUID id = UUID.fromString("0931192f-35a2-461b-bc85-133290c63c6a");

        given()
                .pathParam("id", id)
                .when()
                .get("/customers/{id}/recommendations")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateCustomer() {
        final CustomerRequest request = new CustomerRequest(
                "Erik",
                "Stevenson",
                1010.0,
                "+1 555 555-1234",
                "eriksteve2077@gmail.com",
                "$2a$04$Rwflb1DKTKrgm8bzHac9ke4NO3ki3GA1pGGIWtPUAHODzBPlp1dte"
        );

        var id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/customers")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.toString()))
                .body("content.first_name", is("Erik"))
                .body("content.last_name", is("Stevenson"))
                .body("content.credit", is(1010.0f))
                .body("content.phone", is("+1 555 555-1234"))
                .body("content.email", is("eriksteve2077@gmail.com"))
                .body("content.password", is("$2a$04$Rwflb1DKTKrgm8bzHac9ke4NO3ki3GA1pGGIWtPUAHODzBPlp1dte"));
    }

    @Test
    public void testUpdateCustomerById() {
        final UUID id = UUID.fromString("da8d38a8-28a7-4740-8096-f0197ebabd5c");
        final CustomerRequest request = new CustomerRequest(
                "John",
                "Doe",
                987.81,
                "+1 326 363 8324",
                "john.doe.1999@outlook.com",
                "$2a$04$e66c9HczLQxSiRWqyMN91uIoadNMW86hPi/xq0RGb40P6/Pi0fepu"
        );

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.toString())
                .body(request)
                .when()
                .put("/customers/{id}")
                .then()
                .statusCode(200)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id.toString())
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.toString()))
                .body("content.first_name", is("John"))
                .body("content.last_name", is("Doe"))
                .body("content.credit", is(987.81f))
                .body("content.phone", is("+1 326 363 8324"))
                .body("content.email", is("john.doe.1999@outlook.com"))
                .body("content.password", is("$2a$04$e66c9HczLQxSiRWqyMN91uIoadNMW86hPi/xq0RGb40P6/Pi0fepu"));
    }

    @Test
    public void testUpdateCustomerById_CustomerNotFound() {
        final UUID id = UUID.fromString("bc3bcf03-269c-4a3b-b9ad-99ecc199e4a7");
        final CustomerRequest request = new CustomerRequest(
                "John",
                "Doe",
                987.81,
                "+1 326 363 8324",
                "john.doe.1999@outlook.com",
                "$2a$04$e66c9HczLQxSiRWqyMN91uIoadNMW86hPi/xq0RGb40P6/Pi0fepu"
        );

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.toString())
                .body(request)
                .when()
                .put("/customers/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteCustomerById() {
        final UUID id = UUID.fromString("35645ea7-2b38-430d-aab8-f72302cdc2c8");

        given()
                .pathParam("id", id.toString())
                .when()
                .delete("/customers/{id}")
                .then()
                .statusCode(204);

        given()
                .pathParam("id", id.toString())
                .when()
                .get("/customers/{id}")
                .then()
                .statusCode(404);

        // Verifying cascade of all customer tickets
        given()
                .when()
                .get("/tickets")
                .then()
                .statusCode(200)
                .body("items", not(containsInAnyOrder(id.toString())));
    }
}
