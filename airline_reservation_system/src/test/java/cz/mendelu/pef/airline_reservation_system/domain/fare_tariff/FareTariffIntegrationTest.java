package cz.mendelu.pef.airline_reservation_system.domain.fare_tariff;

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
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class FareTariffIntegrationTest {
    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void testGetFareTariffs() {
        given()
                .when()
                .get("/fare_tariffs")
                .then()
                .statusCode(200)
                .body("count", is(2))
                .body("items[0].id", is(1))
                .body("items[0].code", is("AB99"))
                .body("items[0].business_price", is(5513.0f))
                .body("items[0].premium_price", is(1326.0f))
                .body("items[0].economy_price", is(624.0f))
                .body("items[1].id", is(2))
                .body("items[1].code", is("SK91"))
                .body("items[1].business_price", is(10513.0f))
                .body("items[1].premium_price", is(1726.0f))
                .body("items[1].economy_price", is(737.0f));
    }

    @Test
    public void testGetFareTariffById() {
        final Long id = 1L;

        given()
                .pathParam("id", id)
                .when()
                .get("/fare_tariffs/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.intValue()))
                .body("content.code", is("AB99"))
                .body("content.business_price", is(5513.0f))
                .body("content.premium_price", is(1326.0f))
                .body("content.economy_price", is(624.0f));
    }

    @Test
    public void testGetFareTariffById_NotFound() {
        final Long id = 999999L;

        given()
                .pathParam("id", id)
                .when()
                .get("/fare_tariffs/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateFareTariff() {
        final FareTariffRequest request = new FareTariffRequest("JK89", 9500.0, 1999.0, 524.0);
        final int id = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/fare_tariffs")
                .then()
                .statusCode(201)
                .extract()
                .path("content.id");

        when()
                .get("/fare_tariffs/" + id)
                .then()
                .statusCode(200)
                .body("content.id", is(id))
                .body("content.code", is("JK89"))
                .body("content.business_price", is(9500.0f))
                .body("content.premium_price", is(1999.0f))
                .body("content.economy_price", is(524.0f));
    }

    @Test
    public void testCreateFareTariff_BadRequest() {
        final FareTariffRequest request = new FareTariffRequest("", -9500.0, 1999.0, 524.0);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/fare_tariffs")
                .then()
                .statusCode(400);
    }

    @Test
    public void testUpdateFareTariff() {
        final Long id = 1L;
        final FareTariffRequest request = new FareTariffRequest("AB98", 6500.0, 1699.0, 594.0);

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/fare_tariffs/{id}")
                .then()
                .statusCode(200)
                .body("content.id", is(id.intValue()))
                .body("content.code", is("AB98"))
                .body("content.business_price", is(6500.0f))
                .body("content.premium_price", is(1699.0f))
                .body("content.economy_price", is(594.0f));
    }

    @Test
    public void testUpdateFareTariff_NotFound() {
        final Long id = 999999L;
        final FareTariffRequest request = new FareTariffRequest("AB98", 6500.0, 1699.0, 594.0);

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/fare_tariffs/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdateFareTariff_BadRequest() {
        final Long id = 1L;
        final FareTariffRequest request = new FareTariffRequest("AB98", 6500.0, 1699.0, -594.0);

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/fare_tariffs/{id}")
                .then()
                .statusCode(400);
    }

    // TODO
//    @Test
//    public void deleteFareTariffById() {
//        final Long id = 1L;
//
//        given()
//                .pathParam("id", id)
//                .when()
//                .delete("/fare_tariffs/{id}")
//                .then()
//                .statusCode(204);
//    }
}
