package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public PersonResponse addPerson(PersonRequest person) {
        if (personRepository.isPersonPresent(person)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data");
        }
        return PersonResponse.of(personRepository.addPerson(person));
    }

    @Override
    public PersonResponse findById(String id) {
        if (personRepository.findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        } else
            return PersonResponse.of(personRepository.findById(id));
    }

    @Override
    public PersonResponse updatePerson(String id, PersonRequest person) {
        if (findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found");
        }
        return PersonResponse.of(personRepository.updatePerson(id, person));
    }

    @Override
    public PersonResponse deletePerson(String id) {
        if (findById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid data");
        }
        return PersonResponse.of(personRepository.deletePerson(id));
    }
}
