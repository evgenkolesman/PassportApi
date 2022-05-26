package com.sperasoft.passportapi.controller.resttest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.configuration.EnvConfig;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Description;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = PassportApiApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PassportRestTest {

    @Autowired
    private EnvConfig env;

    private static PassportRequest passportRequest;
    private static PassportResponse passportResponse;
    private static PersonResponse personResponse;
    private static PersonRequest personRequest;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static LocalDate date;
    private static LocalDate datePassport;

    @BeforeAll
    static void testDataProduce() throws JsonProcessingException {
        String string = "2010-02-02";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        datePassport = LocalDate.parse("2022-05-05", format);
        passportRequest = new PassportRequest();
        int number = ThreadLocalRandom.current().nextInt(999999999);
        int departmentCode = ThreadLocalRandom.current().nextInt(99999);
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
    public void testFindPersonPassportWithoutParamsCorrect() {
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() + "/passport/" + passportResponse.getId())
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
    @Order(1)
    public void testFindPersonPassportWithActiveCorrect() {
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" + passportResponse.getId() +
                        "?active=true")
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
        String id = "233414asda";
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() + "/passport/" + id)
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
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" +
                        "?active=true&dateStart=2022-05-04&dateEnd=" + datePassport)
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200);
    }


    @Test
    public void testFindPassportsWithDatesCorrect() {
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" +
                        "?dateStart=2022-05-04&dateEnd=" + datePassport)
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
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" +
                        "?dateStart=2022-05-04&dateEnd=2022-05-01")
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
    @Order(2)
    public void testFindPassportWithoutDatesParamsCorrect() {
        var response = given()
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport" +
                        "?active=true")
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
    public void testFindPassportsWithoutParamsCorrect() throws JsonProcessingException {
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/")
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
    @Order(10)
    public void testLostPassportCorrect() {
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new Description())
                .post("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" + passportResponse.getId() + "/lostPassport?active=false")
                .then()
                .and()
                .log()
                .all()
                .assertThat().statusCode(200);
    }

    @Test
    @Order(11)
    public void testLostPassportNotCorrect() {
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new Description())
                .post("http://localhost:8081/person/" + personResponse.getId() +
                        "/passport/" + passportResponse.getId() + "/lostPassport?active=false")
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
