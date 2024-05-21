package cz.mendelu.pef.airline_reservation_system.domain.airport;

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
public class AirportIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetAirports() {
        given()
                .when()
                .get("/airports")
                .then()
                .statusCode(200)
                .body("count", is(3))
                .body("items[0].code", is("BQK"))
                .body("items[0].name", is("Brunswick Golden Isles Airport"))
                .body("items[0].country_code", is("US"))
                .body("items[0].region_code", is("US-GA"))
                .body("items[0].municipality", is("Brunswick"))
                .body("items[0].gps_code", is("KBQK"))
                .body("items[0].latitude", is(31.255053f))
                .body("items[0].longitude", is(-81.466932f))
                .body("items[1].code", is("TKD"))
                .body("items[1].name", is("Takoradi Airport"))
                .body("items[1].country_code", is("GH"))
                .body("items[1].region_code", is("GH-WP"))
                .body("items[1].municipality", is("Sekondi-Takoradi"))
                .body("items[1].gps_code", is("DGTK"))
                .body("items[1].latitude", is(5.217429f))
                .body("items[1].longitude", is(-1.801527f));
    }

    @Test
    public void testGetAirportById() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .get("/airports/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("BQK"))
                .body("content.name", is("Brunswick Golden Isles Airport"))
                .body("content.country_code", is("US"))
                .body("content.region_code", is("US-GA"))
                .body("content.municipality", is("Brunswick"))
                .body("content.gps_code", is("KBQK"))
                .body("content.latitude", is(31.255053f))
                .body("content.longitude", is(-81.466932f));
    }

    @Test
    public void testGetAirportById_NotFound() {
        final Long id = 999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/airports/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateAirport() {
        final AirportRequest request = new AirportRequest("QRT", "Rieti Airport", "IT", "IT-62", "Rieti", "LIQN", 42.427394, 12.850477);

        var id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/airports")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        given()
                .pathParam("id", id)
                .when()
                .get("/airports/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("QRT"))
                .body("content.name", is("Rieti Airport"))
                .body("content.country_code", is("IT"))
                .body("content.region_code", is("IT-62"))
                .body("content.municipality", is("Rieti"))
                .body("content.gps_code", is("LIQN"))
                .body("content.latitude", is(42.427394f))
                .body("content.longitude", is(12.850477f));
    }

    @Test
    public void testUpdateAirportById() {
        final Long id = 1L;
        final AirportRequest request = new AirportRequest("BQK", "Glynco Jetport", "US", "US-GA", "Brunswick", "KBQK", 31.255053, -81.466932);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .pathParam("id", id)
                .when()
                .put("/airports/{id}")
                .then()
                .statusCode(200);

        given()
                .pathParam("id", id)
                .when()
                .get("/airports/{id}")
                .then()
                .statusCode(200)
                .body("content.code", is("BQK"))
                .body("content.name", is("Glynco Jetport"))
                .body("content.country_code", is("US"))
                .body("content.region_code", is("US-GA"))
                .body("content.municipality", is("Brunswick"))
                .body("content.gps_code", is("KBQK"))
                .body("content.latitude", is(31.255053f))
                .body("content.longitude", is(-81.466932f));
    }

    @Test
    public void testUpdateAirportById_AirportNotFound() {
        final Long id = 999L;
        final AirportRequest request = new AirportRequest("BQK", "Glynco Jetport", "US", "US-GA", "Brunswick", "KBQK", 31.255053, -81.466932);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .pathParam("id", id)
                .when()
                .put("/airports/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteAirportById() {
        final Long id = 3L;

        given()
                .pathParam("id", id)
                .when()
                .delete("/airports/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    public void testDeleteAirportById_FailToDelete_AirportIsInUse() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .delete("/airports/{id}")
                .then()
                .statusCode(409);
    }
}
