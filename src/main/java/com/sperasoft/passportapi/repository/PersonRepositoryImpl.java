package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.model.Person;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Primary
public class PersonRepositoryImpl implements PersonRepository {

    private static final Map<String, Person> personRepo = new ConcurrentHashMap<>();

    @Override
    public List<Person> findAll() {
        return new ArrayList<>(personRepo.values());
    }

    @Override
    public synchronized Person addPerson(Person person) {
        if (findAll().stream().anyMatch(person1 ->
                person1.getName().equals(person.getName())
                        && person1.getBirthday().isEqual(person.getBirthday())
                        && person1.getBirthdayCountry().equals(person.getBirthdayCountry())
        ) || personRepo.containsKey(person.getId())) {
            throw new InvalidPersonDataException();
        }
        personRepo.put(person.getId(), person);
        return person;
    }

    @Override
    public Person findById(String id) {
        if (!personRepo.containsKey(id)) {
            throw new PersonNotFoundException(id);
        }
        return personRepo.get(id);
    }

    @Override
    public synchronized Person updatePerson(Person person) {
        if (!personRepo.containsKey(person.getId())) {
            throw new PersonNotFoundException(person.getId());
        }
        personRepo.replace(person.getId(), person);
        return person;
    }

    @Override
    public synchronized Person deletePerson(String id) {
        if (!personRepo.containsKey(id)) {
            throw new PersonNotFoundException(id);
        }
        return personRepo.remove(id);
    }
}
