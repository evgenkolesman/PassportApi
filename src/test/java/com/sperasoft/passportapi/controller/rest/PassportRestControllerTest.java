package com.sperasoft.passportapi.controller.rest;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import io.restassured.RestAssured;
import io.restassured.internal.http.URIBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassportRestControllerTest {

    private static final String LOST_PASSPORT_URI = "/lostPassport";
    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String PERSON_URI = "/person";
    private static final String PASSPORT_URI = "/passport";

    @Autowired
    private Environment env;
    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int port;

    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;

    @BeforeEach
    void testDataProduce() throws Exception {
        RestAssured.port = port;

        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        LocalDate datePassport = LocalDate.parse("2022-05-05");
        int number = ThreadLocalRandom.current().nextInt(999999999 + 1000000000);
        int departmentCode = ThreadLocalRandom.current().nextInt(99999 + 100000);
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        passportRequest = new PassportRequest(String.valueOf(number),
                datePassport.atStartOfDay().toInstant(ZoneOffset.MIN),
                String.valueOf(departmentCode));
        PersonRequest personRequest = new PersonRequest("Alex Frolov" + varInt, date, "UK");
        String reqPerson = mapper.writeValueAsString(personRequest);
        String reqPassport = mapper.writeValueAsString(passportRequest);

        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPerson)
                .when().post(UriComponentsBuilder
                        .fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        passportResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI).path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PassportResponse.class);
    }

    @AfterEach
    void testDataClear() {
        given().delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                .path(PERSON_URI).path("/")
                .path(personResponse.getId())
                .path(PASSPORT_URI).path("/").path(passportResponse.getId()).toUriString());
        given().delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                .path(PERSON_URI).path("/")
                .path(personResponse.getId())
                .toUriString());

    }

    @Test
    public void testFindPersonPassportWithoutParamsCorrect() {
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId()).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200)
                .extract()
                .body().as(PassportResponse.class);
        assertEquals(passportResponse, response);
    }

    @Test
    public void testFindPersonPassportWithActiveCorrect() {
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId()).queryParam("active", "true").toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200)
                .extract()
                .body().as(PassportResponse.class);
        assertEquals(passportResponse, response);
    }

    @Test
    public void testFindPassportNotCorrectId() {
        String id = FriendlyId.createFriendlyId();
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(id).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(404)
                .extract()
                .response().print();
        assertEquals(String.format(env.getProperty("exception.PassportNotFoundException"), id), response);
    }


    @Test
    public void testFindPassportWithParamsCorrect() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("dateStart", "2022-05-04T19:00:00-02:00");
        params.addIfAbsent("dateEnd", Instant.now().toString());
        params.addIfAbsent("active", "true");
        given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId()).queryParams(params).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200);
    }


    @Test
    public void testFindPassportsWithDatesCorrect() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("dateStart", "2022-05-04T19:00:00-02:00");
        params.addIfAbsent("dateEnd", Instant.now().toString());
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).queryParams(params).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200)
                .extract()
                .body().jsonPath().getList("", PassportResponse.class);

        assertEquals(passportResponse, response.get(0));
    }

    @Test
    public void testFindPassportsWithDatesNotCorrect() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("dateStart", "2022-05-10T19:00:00-02:00");
        params.addIfAbsent("dateEnd", "2022-05-08T19:00:00-02:00");
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).queryParams(params).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(400)
                .extract()
                .response().print();
        assertEquals(String.format(env.getProperty("exception.InvalidPassportDataException"),
                passportResponse.getId()), response);
    }

    @Test
    public void testFindPassportWithoutDatesParamsCorrect() {
        var response = given()
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).queryParam("active", "true").toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getList("", PassportResponse.class);
        assertEquals(passportResponse, response.get(0));
    }

    @Test
    public void testFindPassportsWithoutParamsCorrect() {
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200)
                .extract()
                .body().jsonPath().getList("", PassportResponse.class);

        assertEquals(passportResponse, response.get(0));
    }

    @Test
    public void testLostPassportCorrect() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId())
                        .path(LOST_PASSPORT_URI)
                        .queryParam("active", "false").toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200);
    }

    @Test
    public void testLostPassportNotCorrect() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId())
                        .path(LOST_PASSPORT_URI)
                        .queryParam("active", "false").toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_URI).path("/")
                        .path(passportResponse.getId())
                        .path(LOST_PASSPORT_URI)
                        .queryParam("active", "false").toUriString())
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(409)
                .extract()
                .response().print();
        assertEquals(env.getProperty("exception.PassportDeactivatedException"), response);
    }
}
