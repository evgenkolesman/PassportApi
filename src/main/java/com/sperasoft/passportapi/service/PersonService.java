package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;

public interface PersonService {
    PersonResponse addPerson(PersonRequest person);

    PersonResponse findById(String id);

    PersonResponse updatePerson(String id, PersonRequest person);

    PersonResponse deletePerson(String id);
}
