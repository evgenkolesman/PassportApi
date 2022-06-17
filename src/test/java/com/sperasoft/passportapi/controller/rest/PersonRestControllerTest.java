package com.sperasoft.passportapi.controller.rest;

import com.devskiller.friendly_id.FriendlyId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonRestControllerTest {

    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String PERSON_URI = "/person";

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int port;

    private PersonRequest personRequest;
    private PersonResponse personResponse;
    UriComponentsBuilder builder;

    @BeforeEach
    void testDataProduce() throws Exception {
        builder = UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST).port(port);
        RestAssured.port = port;
        String string = "2010-02-02";
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        int intVar = ThreadLocalRandom.current().nextInt(10000);
        personRequest = new PersonRequest("Alex Frolov" + intVar, date, "UK");
        String req = mapper.writeValueAsString(personRequest);

        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().post(builder.path(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);

    }

    @AfterEach
    public void testDataClear() {
        given()
                .delete(builder.replacePath(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId()).toUriString())
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    void testFindPersonById() {
        String path = builder.replacePath(PERSON_URI)
                .path("/")
                .path(personResponse.getId()).toUriString();
        var response = given()
                .when()
                .get(path)
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
        String id = FriendlyId.createFriendlyId();
        var response = given()
                .when().get(builder.replacePath(PERSON_URI)
                        .path("/")
                        .path(id).toUriString())
                .then()
                .assertThat().statusCode(404)
                .and().log()
                .all()
                .extract().response()
                .body().print();
        assertEquals(String.format(env.getProperty("exception.PersonNotFoundException"), id), response);

    }

    @Test
    void testUpdatePersonByIdCorrect() throws Exception {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        String req = mapper.writeValueAsString(personRequest1);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().put(builder.replacePath(PERSON_URI)
                        .path("/")
                        .path(personResponse.getId()).toUriString())
                .then()
                .and().log()
                .all()
                .assertThat().statusCode(200)
                .extract().body().as(PersonResponse.class);
        assertEquals(personRequest1.getName(), personResponse.getName());
    }

    @Test
    void testUpdatePersonByIdNotCorrect() throws Exception {
        PersonRequest personRequest1 = new PersonRequest("Egor",
                personRequest.getBirthday(),
                personRequest.getBirthdayCountry());
        String req = mapper.writeValueAsString(personRequest1);
        personResponse = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .when().put(builder.replacePath(PERSON_URI).path("/").path(personResponse.getId()).toUriString())
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
                when().post(builder.replacePath(PERSON_URI).toUriString())
                .then()
                .assertThat().statusCode(400).extract().response().body().peek();
        assertEquals(response.print(), env.getProperty("exception.InvalidPersonDataException"));

    }

}
