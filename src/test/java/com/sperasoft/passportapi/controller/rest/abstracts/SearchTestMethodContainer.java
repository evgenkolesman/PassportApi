package com.sperasoft.passportapi.controller.rest.abstracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.NumberPassport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;

@Component
public class SearchTestMethodContainer {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UriComponentsBuilder builder;

    private static final String SEARCHES_ENDPOINT = "/searches";

    public PersonResponse findPersonByPassportNumber(NumberPassport number) throws JsonProcessingException {
        String req = mapper.writer().writeValueAsString(number);
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(req)
                .post(builder
                        .path(SEARCHES_ENDPOINT).toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .body().as(PersonResponse.class);
    }

    public List<PassportResponse> findAllPassports(Boolean active,
                                                   Instant dateStart,
                                                   Instant dateEnd) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addIfAbsent("active", active.toString());
        params.addIfAbsent("dateStart", dateStart.toString());
        params.addIfAbsent("dateEnd", dateEnd.toString());
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .get(builder
                        .path(SEARCHES_ENDPOINT)
                        .queryParams(params)
                        .toUriString())
                .then()
                .and()
                .log()
                .all()
                .statusCode(200)
                .extract()
                .response().body().jsonPath().getList("", PassportResponse.class);
    }

}
