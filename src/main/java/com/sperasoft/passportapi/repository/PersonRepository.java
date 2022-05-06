package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.dto.PersonRequest;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.model.Person;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PersonRepository {

    private Map<String, Person> personRepo = new ConcurrentHashMap<>();

    public List<Person> findAll() {
        return personRepo.values().stream().toList();
    }

    public boolean isPersonPresent(PersonRequest person) {
        return personRepo.values().stream().anyMatch(p -> {
            ModelMapper model = new ModelMapper();
            model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            PersonRequest pr = model.map(p, PersonRequest.class);
            return pr.equals(person);
        });
    }

    public Person addPerson(PersonRequest person) {
        Person personData = Person.of(person);
        personRepo.put(personData.getId(), personData);
        return personData;
    }

    public Person findById(String id) {
        if (!personRepo.containsKey(id)) {
            return null;
        }
        return personRepo.get(id);
    }

    public Person findPersonById(String id) {
        if (!personRepo.containsKey(id)) {
            return null;
        }
        return personRepo.get(id);
    }

    public Person updatePerson(String id, PersonRequest person) {
        Person personData = personRepo.get(id);
        personData.setName(person.getName());
        personData.setBirthday(person.getBirthday());
        personData.setBirthdayCountry(person.getBirthdayCountry());
        personRepo.replace(id, personData);
        return personData;
    }

    public Person deletePerson(String id) {
        return personRepo.remove(id);
    }
}
