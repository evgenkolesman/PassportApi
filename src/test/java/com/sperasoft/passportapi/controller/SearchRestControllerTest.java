package com.sperasoft.passportapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearchRestControllerTest {

    private static final String PERSON_ENDPOINT = "/person";
    private static final String PASSPORT_ENDPOINT = "/passport";
    private static final String SEARCHES_ENDPOINT = "/searches";
    private static final String HTTP_LOCALHOST = "http://localhost";

    @Autowired
    private Environment env;
    @Autowired
    private ObjectMapper mapper;
    private PassportRequest passportRequest;
    private PassportResponse passportResponse;
    private PersonResponse personResponse;
    private PersonRequest personRequest;

    @LocalServerPort
    private int port;
    private int number;

    @BeforeEach
    void testDataProduce() throws Exception {
        RestAssured.port = port;
        LocalDate datePassport = LocalDate.parse("2022-05-05");
        LocalDate date = LocalDate.parse("2022-05-05", DateTimeFormatter.ISO_DATE);
        number = ThreadLocalRandom.current().nextInt(899999999) + 1000000000;
        int departmentCode = ThreadLocalRandom.current().nextInt(899999) + 100000;
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        passportRequest = new PassportRequest(String.valueOf(number),
                datePassport.atStartOfDay().toInstant(ZoneOffset.MIN),
                String.valueOf(departmentCode));
        personRequest = new PersonRequest("Alex Frolov" + varInt, date, "UK");
        String reqPerson = mapper.writeValueAsString(personRequest);
        String reqPassport = mapper.writeValueAsString(passportRequest);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPerson)
                .when().post(UriComponentsBuilder
                        .fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_ENDPOINT).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        passportResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(PERSON_ENDPOINT).path("/")
                        .path(personResponse.getId())
                        .path(PASSPORT_ENDPOINT).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PassportResponse.class);
    }

    @AfterEach
    public void testDataClear() {
        given().delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                .path(PERSON_ENDPOINT).path("/")
                .path(personResponse.getId())
                .path(PASSPORT_ENDPOINT).path("/").path(passportResponse.getId()).toUriString());
        given().delete(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                .path(PERSON_ENDPOINT).path("/")
                .path(personResponse.getId())
                .toUriString());

    }

    @Test
    void testFindPersonByPassportNumberCorrect() throws Exception {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        String req = mapper.writer().writeValueAsString(number1);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .body().as(PersonResponse.class);
        assertEquals(personResponse, response);
    }

    @Test
    void testFindPersonByPassportNumberNotCorrect() throws Exception {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number - 100));
        String req = mapper.writer().writeValueAsString(number1);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(400)
                .extract()
                .response().print();
        assertEquals(env.getProperty("exception.PassportWrongNumberException"), response);
    }

    @Test
    void testFindAllPassports() {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActive() {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT)
                        .queryParam("active", "true").toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithDates() throws Exception {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT)
                        .queryParam("dateStart", "2022-05-01T19:00:00+09:00")
                        .queryParam("dateEnd", "2022-07-01T19:00:00+10:00").toUriString()
                )
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveAndDates() throws Exception {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("dateStart", "2022-05-01T19:00:00+09:00");
        params.addIfAbsent("dateEnd", "2022-07-01T19:00:00+10:00");
        params.addIfAbsent("active", "true");
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT)
                        .queryParams(params).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
        assertEquals(List.of(passportResponse), response);
    }

    @Test
    void testFindAllPassportsWithActiveAndBadDates() throws Exception {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("dateStart", "2022-10-01T19:00:00-10:00");
        params.addIfAbsent("dateEnd", "2022-07-01T19:00:00-10:00");
        params.addIfAbsent("active", "true");
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port)
                        .path(SEARCHES_ENDPOINT)
                        .queryParams(params).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(400)
                .extract()
                .response().print();
        assertEquals(env.getProperty("exception.InvalidPassportDataException"), response);
    }
}
