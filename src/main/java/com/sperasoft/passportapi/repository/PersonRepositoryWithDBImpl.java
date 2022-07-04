package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PersonRepositoryWithDBImpl implements PersonRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ArrayList<Person> findAll() {
        return new ArrayList<>(jdbcTemplate.query("SELECT*FROM passportapi1.public.Person;",
                this::mapToPerson));
    }

    @Override
    public synchronized Person addPerson(Person person) {
        if(findById(person.getId()) == null) {
            jdbcTemplate.update("INSERT INTO passportapi1.public.Person(id, name, birthday, birthdayCountry) " +
                            "values(?, ?, ?, ?);",
                    person.getId(),
                    person.getName(),
                    person.getBirthday(),
                    person.getBirthdayCountry());
        }
        else throw new InvalidPersonDataException();
        return person;
    }



    @Override
    public Person findById(String id) {
        return (Person) jdbcTemplate.query("SELECT*FROM passportapi1.public.Person WHERE id = ?;",
                this::mapToPerson,
                id);
    }

    @Override
    public synchronized Person updatePerson(Person person) {
        if(findById(person.getId()) != null) {
            jdbcTemplate.update("INSERT INTO passportapi1.public.Person(id, name, birthday, birthdayCountry) " +
                            "values(?, ?, ?, ?);",
                    person.getId(),
                    person.getName(),
                    person.getBirthday(),
                    person.getBirthdayCountry());
        }
        else throw new InvalidPersonDataException();
        return person;
    }

    @Override
    public synchronized Person deletePerson(String id) {
        Person person = findById(id);
        if(person != null) {
            jdbcTemplate.update("DELETE FROM passportapi1.public.Person WHERE id = ?;",
                    id);
        }
        else throw new InvalidPersonDataException();
        return person;
    }

    private Person mapToPerson(ResultSet resultSet, int i) throws SQLException {
        return new Person(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate(),
                resultSet.getString("birthdayCountry")
        );
    }
}
