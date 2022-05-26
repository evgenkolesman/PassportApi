package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl {

    private final PersonRepository personRepositoryImpl;


    public PersonResponse addPerson(PersonRequest personRequest) {
        if (isPersonPresent(personRequest)) {
           throw new InvalidPersonDataException();
        }
        Person person = Person.of(personRequest);
        return PersonResponse.of(personRepositoryImpl.addPerson(person));
    }

    public PersonResponse findById(String id) {
        if (personRepositoryImpl.findById(id) == null) {
            throw new PersonNotFoundException(id);
        }
            return PersonResponse.of(personRepositoryImpl.findById(id));
    }

    public PersonResponse updatePerson(String id, PersonRequest personRequest) {
        checkPersonPresentInRepository(id);
        Person person = Person.of(personRequest);
        person.setId(id);
        return PersonResponse.of(personRepositoryImpl.updatePerson(id, person));
    }

    public PersonResponse deletePerson(String id) {
        checkPersonPresentInRepository(id);
        return PersonResponse.of(personRepositoryImpl.deletePerson(id));
    }

    private void checkPersonPresentInRepository(String id) {
        if (findById(id) == null) {
            throw new PersonNotFoundException(id);
        }
    }

    private boolean isPersonPresent(PersonRequest personRequest) {
        return personRepositoryImpl.findAll().stream().anyMatch(p -> {
            PersonRequest pr = ModelMapperMaker.configMapper().map(p, PersonRequest.class);
            return pr.equals(personRequest);
        });
    }
}
