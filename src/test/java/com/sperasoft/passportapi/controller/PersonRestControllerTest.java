package com.sperasoft.passportapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.PassportApiApplication;
import com.sperasoft.passportapi.configuration.EnvConfig;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = PassportApiApplication.class)
public class PersonRestControllerTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static PersonRequest personRequest;
    private static PersonResponse personResponse;

    @Autowired
    private EnvConfig env;

    @BeforeAll
    static void testDataProduce() throws JsonProcessingException {
        String string = "2010-02-02";
        LocalDateTime dateToday = LocalDateTime.now();
        PassportRequest passport = new PassportRequest();
        passport.setNumber("1223123113");
        passport.setGivenDate(dateToday);
        passport.setDepartmentCode("123123");
        personRequest = new PersonRequest();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(string, format);
        int intVar = ThreadLocalRandom.current().nextInt(100);
        personRequest.setName("Alex Frolov" + intVar);
        personRequest.setBirthday(date);
        personRequest.setBirthdayCountry("UK");
        String req = mapper.writeValueAsString(personRequest);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().post("http://localhost:8081/person")
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
    }

    @AfterAll
    public static void testDataClear() {
        given()
                .delete("http://localhost:8081/person/" + personResponse.getId())
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    void testFindPersonById() {
        var response = given()
                .when().get("http://localhost:8081/person/" + personResponse.getId())
                .then()
                .assertThat().statusCode(200)
                .and().log()
                .all()
                .extract().response()
                .body().as(PersonResponse.class);
        assertEquals(response, personResponse);
    }

    @Test
    void testFindPersonByIdNotCorrect() {
        String id = "2332323sd1";
        var response = given()
                .when().get("http://localhost:8081/person/" + id)
                .then()
                .assertThat().statusCode(404)
                .and().log()
                .all()
                .extract().response()
                .body().print();
        assertEquals(String.format(env.getProperty("exception.PersonNotFoundException"), id), response);

    }

    @Test
    void testUpdatePersonByIdCorrect() throws JsonProcessingException {
        PersonRequest personRequest1 = personRequest;
        personRequest1.setName("Egor");
        String req = mapper.writeValueAsString(personRequest1);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().put("http://localhost:8081/person/" + personResponse.getId())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(personRequest1.getName(), personResponse.getName());
    }

    @Test
    void testUpdatePersonByIdNotCorrect() throws JsonProcessingException {
        PersonRequest personRequest1 = personRequest;
        personRequest1.setName("Egor");
        String req = mapper.writeValueAsString(personRequest1);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().put("http://localhost:8081/person/" + personResponse.getId())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(personRequest1.getName(), personResponse.getName());
    }


    @Test
    void testCreatePersonNotCorrect() throws JsonProcessingException {
        String req = mapper.writeValueAsString(personRequest);
        var response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req).
                when().post("http://localhost:8081/person")
                .then()
                .assertThat().statusCode(400).extract().response().body().peek();
        assertEquals(response.print(), env.getProperty("exception.InvalidPersonDataException"));

    }

}
