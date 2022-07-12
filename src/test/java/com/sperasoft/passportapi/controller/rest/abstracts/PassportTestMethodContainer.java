package com.sperasoft.passportapi.controller.rest.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.LostPassportInfo;
import io.restassured.response.ValidatableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.LinkedHashMap;

import static io.restassured.RestAssured.given;

@Component
public class PassportTestMethodContainer {

    @Autowired
    private ObjectMapper mapper;
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
        String reqPassport = mapper.writeValueAsString(passportRequest);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse createPassport(String personId,
                                              String number, Instant givenDate, String departmentCode)
            throws JsonProcessingException {

        LinkedHashMap<String, Object> passportResponseParams = new LinkedHashMap<>();
        passportResponseParams.put("number", number);
        passportResponseParams.put("givenDate", givenDate);
        passportResponseParams.put("departmentCode", departmentCode);

        String path = builder
                .replacePath(PERSON_URI).path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .replaceQuery("").toUriString();
        String reqPassport = mapper.writeValueAsString(passportResponseParams);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post(path)
                .then()
                .and().log()
                .all();
    }

    public ValidatableResponse updatePassport(String personId,
                                              String passportId,
                                              String number, Instant givenDate, String departmentCode) throws JsonProcessingException {
        LinkedHashMap<String, Object> passportResponseParams = new LinkedHashMap<>();
        passportResponseParams.put("number", number);
        passportResponseParams.put("givenDate", givenDate);
        passportResponseParams.put("departmentCode", departmentCode);
        String reqPassport = mapper.writeValueAsString(passportResponseParams);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
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

    public PassportResponse updatePassport(String personId,
                                           String passportId,
                                           PassportRequest passportRequest) throws JsonProcessingException {
        String reqPassport = mapper.writeValueAsString(passportRequest);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(reqPassport)
                .when().post(builder
                        .replacePath(PERSON_URI).path("/")
                        .path(personId)
                        .path(PASSPORT_URI)
                        .path(passportId)
                        .replaceQuery("").toUriString())
                .then()
                .and().log()
                .all()
                .extract()
                .body()
                .as(PassportResponse.class);
    }

    public ValidatableResponse deletePassport(String personId, String passportId) {
        String path = builder
                .replacePath(PERSON_URI).path("/")
                .path(personId)
                .path(PASSPORT_URI)
                .path("/").path(passportId)
                .replaceQuery("").toUriString();
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

    public ValidatableResponse lostPassportDeactivate(String personId,
                                                      String id,
                                                      LostPassportInfo description) throws JsonProcessingException {
        String path = builder
                .replacePath(PERSON_URI)
                .path("/")
                .path(personId)
                .path(PASSPORT_URI).path("/")
                .path(id)
                .path(LOST_PASSPORT_URI)
                .replaceQuery("").toUriString();
        if (description == null) description = new LostPassportInfo("");
        String message = mapper.writeValueAsString(description.getDescription());
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(message)
                .when()
                .post(path)
                .then()
                .and()
                .log()
                .all();
    }
}
