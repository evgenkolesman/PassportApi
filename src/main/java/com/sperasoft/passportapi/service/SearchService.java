package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PersonResponse;

public interface SearchService {
    PersonResponse findPersonByPassportNumber(String number);
}
