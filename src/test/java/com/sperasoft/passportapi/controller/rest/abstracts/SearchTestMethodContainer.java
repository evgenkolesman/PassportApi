package com.sperasoft.passportapi.controller.rest.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.model.NumberPassport;
import io.restassured.response.ValidatableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

import static io.restassured.RestAssured.given;

@Component
public class SearchTestMethodContainer {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UriComponentsBuilder builder;

    private static final String SEARCHES_ENDPOINT = "/searches";

    public ValidatableResponse findPersonByPassportNumber(NumberPassport number) throws JsonProcessingException {
        String req = mapper.writer().writeValueAsString(number);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post(builder
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
                .get(builder
                        .replacePath(SEARCHES_ENDPOINT)
                        .replaceQueryParams(params)
                        .toUriString())
                .then()
                .and()
                .log()
                .all();
    }

}
