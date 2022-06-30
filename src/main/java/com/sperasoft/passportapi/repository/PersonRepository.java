package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.model.Person;

import java.util.List;

public interface PersonRepository {
    List<Person> findAll();

    Person addPerson(Person person);

    Person findById(String id);

    Person updatePerson(String id, Person person);

    Person deletePerson(String id);
}
