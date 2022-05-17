package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PersonRequest;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl {

    private final PersonRepository personRepositoryImpl;
    private final Environment environment;

    public boolean isPersonPresent(PersonRequest personRequest) {
        return personRepositoryImpl.findAll().stream().anyMatch(p -> {
            PersonRequest pr = ModelMapperMaker.configMapper().map(p, PersonRequest.class);
            return pr.equals(personRequest);
        });
    }
    public PersonResponse addPerson(PersonRequest personRequest) {
        if (isPersonPresent(personRequest)) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.BAD_REQUEST,
                    Objects.requireNonNull(environment.getProperty("person.exception.invalid-data"))));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    environment.getProperty("person.exception.invalid-data"));
        }
        Person person = Person.of(personRequest);
        return PersonResponse.of(personRepositoryImpl.addPerson(person));
    }

    public PersonResponse findById(String id) {
        if (personRepositoryImpl.findById(id) == null) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(environment.getProperty("person.exception.notfound")), id)));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(
                            environment.getProperty("person.exception.notfound")), id));
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
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(environment.getProperty("person.exception.notfound")), id)));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(
                            environment.getProperty("person.exception.notfound")), id));
        }
    }
}
