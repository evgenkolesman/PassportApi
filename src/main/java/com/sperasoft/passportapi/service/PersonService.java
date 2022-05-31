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

    private final PersonRepository personRepositoryImpl;


    public Person addPerson(Person person) {
        if (personRepositoryImpl.findAll().stream().anyMatch(person1 ->
                person1.getName().equals(person.getName())
                        && person1.getBirthday().isEqual(person.getBirthday())
                        && person1.getBirthdayCountry().equals(person.getBirthdayCountry())
        )) {
           throw new InvalidPersonDataException();
        }
        return personRepositoryImpl.addPerson(person);
    }

    public Person findById(String id) {
        if (personRepositoryImpl.findById(id) == null) {
            throw new PersonNotFoundException(id);
        }
            return personRepositoryImpl.findById(id);
    }

    public Person updatePerson(String id, Person person) {
        checkPersonPresentInRepository(id);
        person.setId(id);
        return personRepositoryImpl.updatePerson(id, person);
    }

    public Person deletePerson(String id) {
        checkPersonPresentInRepository(id);
        return personRepositoryImpl.deletePerson(id);
    }

    private void checkPersonPresentInRepository(String id) {
        if (findById(id) == null) {
            throw new PersonNotFoundException(id);
        }
    }
}
