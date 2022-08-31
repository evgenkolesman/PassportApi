package com.sperasoft.passportapi.utils;

import org.springframework.web.util.UriComponentsBuilder;

public class UriComponentsBuilderUtil {

    private static final String HTTP_LOCALHOST = "http://localhost";

    public static UriComponentsBuilder builder() {
        return UriComponentsBuilder
                .fromHttpUrl(HTTP_LOCALHOST);
    }
}
