package com.sperasoft.passportapi.controller.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequestTest;
import io.restassured.response.ValidatableResponse;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static io.restassured.RestAssured.given;

@Component
//@SpringBootTest

public class PersonTestMethodContainer {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private UriComponentsBuilder builder;

    private static final String PERSON_URI = "/person";

    public ValidatableResponse createPerson(String name, String birthday, String birthdayCountry) throws JsonProcessingException, JSONException {
        PersonRequestTest personRequestTest = new PersonRequestTest(name, birthday, birthdayCountry);
        String message = mapper.writeValueAsString(personRequestTest);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(message)
                .when().post(builder
                        .replacePath(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse createPerson(PersonRequest personRequest) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(personRequest)
                .when().post(builder
                        .replacePath(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse updatePerson(String personId,
                                            PersonRequest personRequestUpdate) throws JsonProcessingException {
        String path = builder.replacePath(PERSON_URI)
                .path("/")
                .path(personId).toUriString();
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(personRequestUpdate)
                .when().put(path)
                .then()
                .and().log()
                .all();
    }


    public ValidatableResponse updatePerson(String personId,
                                            PersonRequestTest personRequestTest) throws JsonProcessingException {
        String message = mapper.writeValueAsString(personRequestTest);
        String path = builder.replacePath(PERSON_URI)
                .path("/")
                .path(personId).toUriString();
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(message)
                .when().put(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse deletePerson(String personId) {
        String path = builder.replacePath(PERSON_URI)
                .path("/")
                .path(personId).toUriString();
        return given()
                .delete(path)
                .then()
                .log().all();
    }

    public ValidatableResponse findPersonById(String id) {
        String path = builder.replacePath(PERSON_URI)
                .path("/")
                .path(id).toUriString();
        return given()
                .when()
                .get(path)
                .then()
                .and().log()
                .all();
    }


}
