package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;

import java.time.Instant;
import java.util.List;

public interface PassportRepository {

    Passport addPassport(Passport passport, Person person);

    Passport findPassportById(String id);

    Passport findPassportById(String id, boolean active);

    Passport updatePassport(Person person, Passport passport);

    Passport deletePassport(String id);

    List<Passport> getPassportsByParams(Boolean active, Instant dateStart, Instant dateEnd);

    List<Passport> getPassportsByParams(Instant dateStart, Instant dateEnd);

    List<Passport> getPassportsByParams(Boolean active);

    List<Passport> getPassportsByParams();
}
