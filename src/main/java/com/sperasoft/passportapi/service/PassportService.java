package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;

import java.text.ParseException;
import java.util.List;

public interface PassportService {

    PassportResponse addPassportToPerson(String personId, PassportRequest passportRequest);

    PassportResponse findPassportById(String personId, String id, String active);

    PassportResponse updatePassport(String personId, String id, PassportRequest passport);

    PassportResponse deletePassport(String personId, String id);

    List<PassportResponse> getPassportsByPersonIdAndParams(String personId, String active,
                                                           String dateStart, String dateEnd) throws ParseException;
}
