package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.personexceptions.InvalidPersonDataException;
import com.sperasoft.passportapi.model.Passport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PassportRepositoryWithDBImpl implements PassportRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public synchronized Passport addPassport(Passport passport) {
        if(findPassportById(passport.getId()) == null) {
            jdbcTemplate.update(
        "INSERT INTO passportapi1.public.Passport(id, number, givenDate, departmentCode, active, description, person_id) " +
                            "values(?, ?, ?, ?, ?, ?, ?);",
                    passport.getId(),
                    passport.getNumber(),
                    passport.getGivenDate(),
                    passport.getDepartmentCode(),
                    passport.isActive(),
                    passport.getDescription(),
                    passport.getPersonId());
        }
        else throw new InvalidPassportDataException();
        return passport;
    }

    @Override
    public synchronized Passport updatePassport(Passport passport) {
        if(findPassportById(passport.getId()) != null) {
            jdbcTemplate.update(
                    "INSERT INTO passportapi1.public.Passport(id, number, givenDate, departmentCode, active, description, person_id) " +
                            "values(?, ?, ?, ?, ?, ?, ?);",
                    passport.getId(),
                    passport.getNumber(),
                    passport.getGivenDate(),
                    passport.getDepartmentCode(),
                    passport.isActive(),
                    passport.getDescription(),
                    passport.getPersonId());
        }
        else throw new InvalidPassportDataException();
        return passport;
    }

    @Override
    public synchronized Passport deletePassport(String id) {
        Passport passport = findPassportById(id);
        if(passport != null) {
            jdbcTemplate.update("DELETE FROM passportapi1.public.Passport WHERE id = ?;",
                    id);
        }
        else throw new InvalidPersonDataException();
        return passport;
    }

    @Override
    public Passport findPassportById(String id) {
        return (Passport) jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE id = ?;",
                this::mapToPassport,
                id);
    }

    @Override
    public Passport findPassportById(String id, boolean active) {
        return (Passport) jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE id = ? AND active = ?;",
                this::mapToPassport,
                id, active);
    }


    @Override
    public ArrayList<Passport> getPassportsByParams() {
        return new ArrayList<>(jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport;",
                this::mapToPassport));
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Boolean active, Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE person_id = ? AND active = ? AND givendate BETWEEN ? AND ?;",
                this::mapToPassport, personId, active, startDate, endDate));
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE person_id = ? AND givendate BETWEEN ? AND ?;",
                                this::mapToPassport, personId, startDate, endDate));
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Boolean active) {
        return jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE person_id = ? AND active = ?;",
                this::mapToPassport,
                personId, active);
    }

    @Override
    public List<Passport> getPassportsByParams(String personId) {
        return jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE person_id = ?;",
                this::mapToPassport,
                personId);
    }

    @Override
    public List<Passport> getPassportsByParams(Boolean active, Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE active = ? AND givendate BETWEEN ? AND ?;",
                                this::mapToPassport, active, startDate, endDate));
    }

    @Override
    public List<Passport> getPassportsByParams(Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE givendate BETWEEN ? AND ?;",
                                this::mapToPassport,  startDate, endDate));
    }

    @Override
    public List<Passport> getPassportsByParams(Boolean active) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE active = ?;",
                                this::mapToPassport, active));
    }

    @Override
    public Passport getPassportByNumber(String number) {
        return (Passport) jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE number = ?;",
                                this::mapToPassport, number);
    }

    private Passport mapToPassport(ResultSet resultSet, int i) throws SQLException {
        return new Passport(
                resultSet.getString("id"),
                resultSet.getString("personId"),
                resultSet.getString("number"),
                resultSet.getDate("givenDate").toInstant(),
                resultSet.getString("departmentCode"),
                resultSet.getBoolean("active"),
                resultSet.getString("description")
        );
    }
}
