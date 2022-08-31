package com.sperasoft.passportapi.controller.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportRequestTestModel;
import com.sperasoft.passportapi.controller.dto.TestLostPassportInfo;
import com.sperasoft.passportapi.utils.UriComponentsBuilderUtil;
import io.restassured.response.ValidatableResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static io.restassured.RestAssured.given;

@Component
public class PassportTestMethodContainer {

    private static final String LOST_PASSPORT_URI = "/lostPassport";
    private static final String PERSON_URI = "/person";
    private static final String PASSPORT_URI = "/passport";

    public ValidatableResponse createPassport(String personId,
                                              PassportRequest passportRequest) throws JsonProcessingException {
        String path = UriComponentsBuilderUtil.builder()
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
        String path = UriComponentsBuilderUtil.builder()
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
                                              String number, String givenDate, String departmentCode) {
        PassportRequestTestModel passportRequestTest = new PassportRequestTestModel(number, givenDate, departmentCode);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequestTest)
                .when().put(UriComponentsBuilderUtil.builder()
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
                                              PassportRequest passportRequest) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(passportRequest)
                .when().put(UriComponentsBuilderUtil.builder()
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
        String path = UriComponentsBuilderUtil.builder()
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
        String path = UriComponentsBuilderUtil.builder()
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
        String path = UriComponentsBuilderUtil.builder()
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

    public ValidatableResponse lostPassportDeactivate(String personId,
                                                      String id,
                                                      TestLostPassportInfo description) {
        String path = UriComponentsBuilderUtil.builder()
                .replacePath(PERSON_URI)
                .path("/")
                .path(personId)
                .path(PASSPORT_URI).path("/")
                .path(id)
                .path(LOST_PASSPORT_URI)
                .replaceQuery("").toUriString();
        if (description == null) description = new TestLostPassportInfo("");
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(description)
                .when()
                .post(path)
                .then()
                .and()
                .log()
                .all();
    }
}
