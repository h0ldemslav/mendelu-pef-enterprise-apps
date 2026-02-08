package cz.mendelu.pef.airline_reservation_system.domain.aircraft;

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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class AircraftIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetAircrafts() {
        given()
                .when()
                .get("/aircrafts")
                .then()
                .statusCode(200)
                .body("count", is(2))
                .body("items[0].code", is("SI006"))
                .body("items[0].model", is("Airbus A380"))
                .body("items[0].business_capacity", is(3))
                .body("items[0].premium_capacity", is(16))
                .body("items[0].economy_capacity", is(125))
                .body("items[1].code", is("BK007"))
                .body("items[1].model", is("Boeing 737"))
                .body("items[1].business_capacity", is(6))
                .body("items[1].premium_capacity", is(18))
                .body("items[1].economy_capacity", is(180));
    }

    @Test
    public void testGetAircraftById() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .get("/aircrafts/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("SI006"))
                .body("content.model", is("Airbus A380"))
                .body("content.business_capacity", is(3))
                .body("content.premium_capacity", is(16))
                .body("content.economy_capacity", is(125));
    }

    @Test
    public void testGetAircraftById_NotFound() {
        final Long id = 999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/aircrafts/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateAircraft() {
        final AircraftRequest request = new AircraftRequest("JJ890", "Airbus A350", 10, 20, 230);

        var id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/aircrafts")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/aircrafts/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("JJ890"))
                .body("content.model", is("Airbus A350"))
                .body("content.business_capacity", is(10))
                .body("content.premium_capacity", is(20))
                .body("content.economy_capacity", is(230));
    }

    @Test
    public void testUpdateAircraftById() {
        final Long id = 1L;
        final AircraftRequest request = new AircraftRequest("AA100", "Airbus A350", 12, 21, 220);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .pathParam("id", id)
                .when()
                .put("/aircrafts/{id}")
                .then()
                .statusCode(200);

        given()
                .pathParam("id", id)
                .when()
                .get("/aircrafts/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("AA100"))
                .body("content.model", is("Airbus A350"))
                .body("content.business_capacity", is(12))
                .body("content.premium_capacity", is(21))
                .body("content.economy_capacity", is(220));
    }

    @Test
    public void testUpdateAircraftById_AircraftNotFound() {
        final Long id = 999L;
        final AircraftRequest request = new AircraftRequest("AA100", "Airbus A350", 12, 21, 220);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .pathParam("id", id)
                .when()
                .put("/aircrafts/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteAircraftById() {
        final Long id = 2L;

        given()
                .pathParam("id", id)
                .when()
                .delete("/aircrafts/{id}")
                .then()
                .statusCode(204);
    }
}
