package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public synchronized Person addPerson(Person person) {
        if (findById(person.getId()) == null) {
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
        return result.size() == 0 ? null : result.get(0);
    }

    @Override
    public synchronized Person updatePerson(Person person) {
        if (findById(person.getId()) != null) {
            jdbcTemplate.update(
                    "UPDATE passportapi1.public.Person SET name = ?, birthday = ?, birthdayCountry = ? where id =?;",
                    person.getName(),
                    person.getBirthday(),
                    person.getBirthdayCountry(),
                    person.getId()
            );
        } else throw new InvalidPersonDataException();
        return person;
    }

    @Override
    public synchronized Person deletePerson(String id) {
        Person person = findById(id);
        if (person != null) {
            jdbcTemplate.update("DELETE FROM passportapi1.public.Person WHERE id = ?;",
                    id);
        } else throw new InvalidPersonDataException();
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
