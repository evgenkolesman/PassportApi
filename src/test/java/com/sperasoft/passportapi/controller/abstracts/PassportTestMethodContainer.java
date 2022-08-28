package com.sperasoft.passportapi.controller.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportRequestTestModel;
import com.sperasoft.passportapi.controller.dto.TestLostPassportInfo;
import com.sperasoft.passportapi.model.LostPassportInfo;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

import static io.restassured.RestAssured.given;

@Component
public class PassportTestMethodContainer {


    @Autowired
    private UriComponentsBuilder builder;

    private static final String LOST_PASSPORT_URI = "/lostPassport";
    private static final String PERSON_URI = "/person";
    private static final String PASSPORT_URI = "/passport";

    public ValidatableResponse createPassport(String personId,
                                              PassportRequest passportRequest) throws JsonProcessingException {
        String path = builder
                .replacePath(PERSON_URI).path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .replaceQuery("").toUriString();
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequest)
                .when().post(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse createPassport(String personId,
                                              String number, String givenDate, String departmentCode)
            throws JsonProcessingException {
        PassportRequestTestModel passportRequest = new PassportRequestTestModel(number, givenDate, departmentCode);
        String path = builder
                .replacePath(PERSON_URI).path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .replaceQuery("").toUriString();
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequest)
                .when().post(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse updatePassport(String personId,
                                              String passportId,
                                              String number, String givenDate, String departmentCode) throws JsonProcessingException {
        PassportRequestTestModel passportRequestTest = new PassportRequestTestModel(number, givenDate, departmentCode);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequestTest)
                .when().put(builder
                        .replacePath(PERSON_URI).path("/")
                        .path(personId)
                        .path(PASSPORT_URI).path("/")
                        .path(passportId)
                        .replaceQuery("").toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse updatePassport(String personId,
                                              String passportId,
                                              PassportRequest passportRequest) throws JsonProcessingException {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequest)
                .when().put(builder
                        .replacePath(PERSON_URI).path("/")
                        .path(personId)
                        .path(PASSPORT_URI).path("/")
                        .path(passportId)
                        .replaceQuery("").toUriString())
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse deletePassport(String personId, String passportId) {
        String path = builder
                .replacePath(PERSON_URI).path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .path("/").path(passportId).toUriString();
        return given()
                .delete(path)
                .then()
                .log().all();
    }

    public ValidatableResponse findPersonPassports(String personId,
                                                   Boolean active,
                                                   Instant dateStart,
                                                   Instant dateEnd) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (active != null || dateStart != null || dateEnd != null) {
            if (active != null) params.addIfAbsent("active", String.valueOf(active));
            if (dateStart != null || dateEnd != null) {
                if (dateStart != null) params.addIfAbsent("dateStart", String.valueOf(dateStart));
                if (dateEnd != null) params.addIfAbsent("dateEnd", String.valueOf(dateEnd));
            }

        }
        String path = builder
                .replacePath(PERSON_URI)
                .path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .replaceQueryParams(params).toUriString();
        return given()
                .get(path)
                .then()
                .log()
                .all();
    }

    public ValidatableResponse findPassport(String personId,
                                            String passportId,
                                            @Nullable Boolean active) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (active != null) params.addIfAbsent("active", String.valueOf(active));
        String path = builder
                .replacePath(PERSON_URI)
                .path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .path("/")
                .path(passportId)
                .replaceQueryParams(params).toUriString();
        return given()
                .get(path)
                .then()
                .and()
                .log()
                .all();
    }

    //TODO !!!! Fix description

    public ValidatableResponse lostPassportDeactivate(String personId,
                                                      String id,
                                                      TestLostPassportInfo description) throws JsonProcessingException {
        String path = builder
                .replacePath(PERSON_URI)
                .path("/")
                .path(personId)
                .path(PASSPORT_URI).path("/")
                .path(id)
                .path(LOST_PASSPORT_URI)
                .replaceQuery("").toUriString();
        if (description == null) description = new TestLostPassportInfo("");
        String message = new ObjectMapper().writeValueAsString(description.getDescription());
        System.out.println("-----> " + message);

        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .body(new ObjectMapper().writeValueAsString(description))
                .body("\"" + description.getDescription() + "\"")
                .when()
                .post(path)
                .then()
                .and()
                .log()
                .all();
    }
}
