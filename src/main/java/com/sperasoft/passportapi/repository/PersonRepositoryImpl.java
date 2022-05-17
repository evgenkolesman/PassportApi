package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.model.Person;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PersonRepositoryImpl implements PersonRepository{

    private final Map<String, Person> personRepo = new ConcurrentHashMap<>();

    @Override
    public List<Person> findAll() {
        return new ArrayList<>(personRepo.values());
    }

    @Override
    public Person addPerson(Person person) {
        personRepo.put(person.getId(), person);
        return person;
    }

    @Override
    public Person findById(String id) {
        if (!personRepo.containsKey(id)) {
            return null;
        }
        return personRepo.get(id);
    }

    @Override
    public Person findPersonById(String id) {
        if (!personRepo.containsKey(id)) {
            return null;
        }
        return personRepo.get(id);
    }

    @Override
    public Person updatePerson(String id, Person person) {
        personRepo.replace(id, person);
        return person;
    }

    @Override
    public Person deletePerson(String id) {
        return personRepo.remove(id);
    }
}
