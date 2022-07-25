package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
//@Profile(value = "dev")
@Primary
public class PersonRepositoryImplDB implements PersonRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ArrayList<Person> findAll() {
        return new ArrayList<>(jdbcTemplate.query("SELECT*FROM passportapi1.public.Person;",
                this::mapToPerson));
    }

    //TODO add unique field comprehansion

    @Override
    public synchronized Person addPerson(Person person) {
        if (!checkPresentById(person.getId()) &&
                findByParamsWithoutId(person.getName(),
                        person.getBirthday(),
                        person.getBirthdayCountry())) {
            jdbcTemplate.update("INSERT INTO passportapi1.public.Person(id, name, birthday, birthdayCountry) " +
                            "values(?, ?, ?, ?);",
                    person.getId(),
                    person.getName(),
                    person.getBirthday(),
                    person.getBirthdayCountry());
        } else throw new InvalidPersonDataException();
        return person;
    }

    @Override
    public Person findById(String id) {
        List<Person> result = jdbcTemplate.query("SELECT*FROM passportapi1.public.Person WHERE id = ?;",
                this::mapToPerson,
                id);
        if (result.size() == 0) throw new PersonNotFoundException(id);
        return result.get(0);
    }

    @Override
    public synchronized Person updatePerson(Person person) {
        if (checkPresentById(person.getId())) {
            jdbcTemplate.update(
                    "UPDATE passportapi1.public.Person SET name = ?, birthday = ?, birthdayCountry = ? where id =?;",
                    person.getName(),
                    person.getBirthday(),
                    person.getBirthdayCountry(),
                    person.getId()
            );
        } else throw new PersonNotFoundException(person.getId());
        return person;
    }

    @Override
    public synchronized Person deletePerson(String id) {
        Person person;
        if (checkPresentById(id)) {
            person = findById(id);
            jdbcTemplate.update("DELETE FROM passportapi1.public.Person CASCADE WHERE id = ?;",
                    id);
        } else throw new PersonNotFoundException(id);
        return person;
    }

    private boolean checkPresentById(String id) {
        List<Person> result = jdbcTemplate.query("SELECT*FROM passportapi1.public.Person WHERE id = ?;",
                this::mapToPerson,
                id);
        return result.size() != 0;
    }

    private Person mapToPerson(ResultSet resultSet, int i) throws SQLException {
        return new Person(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate(),
                resultSet.getString("birthdayCountry")
        );
    }

    private boolean findByParamsWithoutId(String name, LocalDate birthday, String birthdayCountry) {
        return jdbcTemplate.query("SELECT*FROM passportapi1.public.Person where name = ? " +
                        "AND birthday = ? " +
                        "AND birthdaycountry = ?;",
                this::mapToPerson,
                name,
                birthday,
                birthdayCountry
        ).size() == 0;
    }
}
