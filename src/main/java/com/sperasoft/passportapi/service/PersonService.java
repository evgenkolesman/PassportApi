package com.sperasoft.passportapi.service;

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
public class PersonService {

    private final PersonRepository personRepository;


    public Person addPerson(Person person) {
        return personRepository.addPerson(person);
    }

    public Person findById(String id) {
            return personRepository.findById(id);
    }

    public Person updatePerson(Person person) {
        return personRepository.updatePerson(person);
    }

    public Person deletePerson(String id) {
        return personRepository.deletePerson(id);
    }
}
