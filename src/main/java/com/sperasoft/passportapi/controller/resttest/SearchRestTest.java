package com.sperasoft.passportapi.controller.resttest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.configuration.EnvConfig;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = PassportApiApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SearchRestTest {

    @Autowired
    private EnvConfig env;

    private static PassportRequest passportRequest;
    private static PassportResponse passportResponse;
    private static PersonResponse personResponse;
    private static PersonRequest personRequest;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static LocalDate date;
    private static LocalDate datePassport;
    private static int number;

    @BeforeAll
    static void testDataProduce() throws JsonProcessingException {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        datePassport = LocalDate.parse("2022-05-05", format);
        passportRequest = new PassportRequest();
        number = ThreadLocalRandom.current().nextInt(899999999) + 1000000000;
        int departmentCode = ThreadLocalRandom.current().nextInt(899999) + 100000;
        int varInt = ThreadLocalRandom.current().nextInt(10000000);
        passportRequest.setNumber(String.valueOf(number));
        passportRequest.setGivenDate(datePassport);
        passportRequest.setDepartmentCode(String.valueOf(departmentCode));
        PersonRequest personRequest = new PersonRequest();
        date = LocalDate.parse(string, format);
        personRequest.setName("Alex Frolov" + varInt);
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");

        String reqPerson = mapper.writeValueAsString(personRequest);
        String reqPassport = mapper.writeValueAsString(passportRequest);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPerson)
                .when().post("http://localhost:8081/person")
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        passportResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post("http://localhost:8081/person/" + personResponse.getId() + "/passport")
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PassportResponse.class);
    }

    @AfterAll
    public static void testDataClear() {
        given().delete("http://localhost:8081/person/" + personResponse.getId())
                .then().assertThat().statusCode(204);
        given().delete("http://localhost:8081/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
                .then().assertThat().statusCode(204);
    }

    @Test
    void testFindPersonByPassportNumberCorrect() throws JsonProcessingException {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        String req = mapper.writer().writeValueAsString(number1);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post("http://localhost:8081/searches")
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
    void testFindPersonByPassportNumberNotCorrect() throws JsonProcessingException {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number - 100));
        String req = mapper.writer().writeValueAsString(number1);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post("http://localhost:8081/searches")
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
                .get("http://localhost:8081/searches")
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
                .get("http://localhost:8081/searches?active=true")
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
    void testFindAllPassportsWithDates() {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("http://localhost:8081/searches?dateStart=2022-05-04&dateEnd=2022-05-06")
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
    void testFindAllPassportsWithActiveAndDates() {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("http://localhost:8081/searches?active=true&dateStart=2022-05-04&dateEnd=2022-05-06")
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
    void testFindAllPassportsWithActiveAndBadDates() {
        var number1 = new NumberPassport();
        number1.setNumber(String.valueOf(number));
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("http://localhost:8081/searches?active=true&dateStart=2022-05-09&dateEnd=2022-05-06")
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
