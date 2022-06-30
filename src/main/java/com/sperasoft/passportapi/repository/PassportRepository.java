package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;

import java.time.Instant;
import java.util.List;

public interface PassportRepository {

    Passport addPassport(Passport passport);

    Passport findPassportById(String id);

    Passport findPassportById(String id, boolean active);

    Passport updatePassport(Passport passport);

    Passport deletePassport(String id);

    List<Passport> getPassportsByParams();

    List<Passport> getPassportsByParams(String personId, Boolean active, Instant startDate, Instant endDate);

    List<Passport> getPassportsByParams(String personId, Instant startDate, Instant endDate);

    List<Passport> getPassportsByParams(String personId, Boolean active);

    List<Passport> getPassportsByParams(String personId);

    List<Passport> getPassportsByParams(Boolean active, Instant dateStart, Instant dateEnd);

    List<Passport> getPassportsByParams(Instant dateStart, Instant dateEnd);

    List<Passport> getPassportsByParams(Boolean active);

    Passport getPassportByNumber(String number);
}


