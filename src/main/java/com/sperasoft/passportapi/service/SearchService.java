package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonResponse;

import java.text.ParseException;
import java.util.List;

public interface SearchService {
    PersonResponse findPersonByPassportNumber(String number);

    List<PassportResponse> getAllPassports(String active, String dateStart, String dateEnd) throws ParseException;
}
