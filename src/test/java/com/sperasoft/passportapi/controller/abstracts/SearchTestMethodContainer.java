package com.sperasoft.passportapi.controller.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.model.Number;
import com.sperasoft.passportapi.utils.UriComponentsBuilderUtil;
import io.restassured.response.ValidatableResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static io.restassured.RestAssured.given;

@Component
public class SearchTestMethodContainer {

    private static final String SEARCHES_ENDPOINT = "/searches";

    public ValidatableResponse findPersonByPassportNumber(Number number) throws JsonProcessingException {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ObjectMapper().writer().writeValueAsString(number))
                .post(UriComponentsBuilderUtil.builder()
                        .replacePath(SEARCHES_ENDPOINT).toUriString())
                .then()
                .and()
                .log()
                .all();
    }

    public ValidatableResponse findAllPassports(Boolean active,
                                                Instant dateStart,
                                                Instant dateEnd) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (active != null) params.addIfAbsent("active", active.toString());
        if (dateStart != null) params.addIfAbsent("dateStart", dateStart.toString());
        if (dateEnd != null) params.addIfAbsent("dateEnd", dateEnd.toString());
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilderUtil
                        .builder()
                        .replacePath(SEARCHES_ENDPOINT)
                        .replaceQueryParams(params)
                        .toUriString())
                .then()
                .and()
                .log()
                .all();
    }


    public ValidatableResponse findAllPassportsWithString(String active,
                                                          String dateStart,
                                                          String dateEnd) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (active != null) params.addIfAbsent("active", active);
        if (dateStart != null) params.addIfAbsent("dateStart", dateStart);
        if (dateEnd != null) params.addIfAbsent("dateEnd", dateEnd);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(UriComponentsBuilderUtil.builder()
                        .replacePath(SEARCHES_ENDPOINT)
                        .replaceQueryParams(params)
                        .toUriString())
                .then()
                .and()
                .log()
                .all();
    }

}
