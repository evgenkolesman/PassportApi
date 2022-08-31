package com.sperasoft.passportapi.controller.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonRequestTestModel;
import com.sperasoft.passportapi.utils.UriComponentsBuilderUtil;
import io.restassured.response.ValidatableResponse;
import org.json.JSONException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static io.restassured.RestAssured.given;

@Component
public class PersonTestMethodContainer {

    private static final String PERSON_URI = "/person";

    public ValidatableResponse createPerson(String name, String birthday, String birthdayCountry) throws JsonProcessingException, JSONException {
        PersonRequestTestModel personRequestTest = new PersonRequestTestModel(name, birthday, birthdayCountry);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(personRequestTest)
                .when().post(UriComponentsBuilderUtil.builder()
                        .replacePath(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse createPerson(PersonRequest personRequest) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(personRequest)
                .when().post(UriComponentsBuilderUtil.builder()
                        .replacePath(PERSON_URI).toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse updatePerson(String personId,
                                            PersonRequest personRequestUpdate) {
        String path = UriComponentsBuilderUtil.builder().replacePath(PERSON_URI)
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
                                            PersonRequestTestModel personRequestTest) {
        String path = UriComponentsBuilderUtil.builder().replacePath(PERSON_URI)
                .path("/")
                .path(personId).toUriString();
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(personRequestTest)
                .when().put(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse deletePerson(String personId) {
        String path = UriComponentsBuilderUtil.builder().replacePath(PERSON_URI)
                .path("/")
                .path(personId).toUriString();
        return given()
                .delete(path)
                .then()
                .log().all();
    }

    public ValidatableResponse findPersonById(String id) {
        String path = UriComponentsBuilderUtil.builder().replacePath(PERSON_URI)
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
