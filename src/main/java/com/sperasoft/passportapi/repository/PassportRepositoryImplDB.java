package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportEmptyException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportNotFoundException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.model.Passport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Primary
public class PassportRepositoryImplDB implements PassportRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public synchronized Passport addPassport(Passport passport) {
        if (findPassportById(passport.getId()) == null) {
            jdbcTemplate.update(
                    "INSERT INTO passportapi1.public.Passport(id, number, givenDate, departmentCode, active, description, person_id) " +
                            "values(?, ?, ?, ?, ?, ?, ?);",
                    passport.getId(),
                    passport.getNumber(),
                    Timestamp.from(passport.getGivenDate()),
                    passport.getDepartmentCode(),
                    passport.isActive(),
                    passport.getDescription(),
                    passport.getPersonId());
        } else throw new InvalidPassportDataException();
        return passport;
    }

    @Override
    public synchronized Passport updatePassport(Passport passport) {
        if (findPassportById(passport.getId()) != null) {
            jdbcTemplate.update(
                    "UPDATE passportapi1.public.Passport SET number = ?, " +
                            "givenDate = ? , departmentCode = ?, active = ?, description = ?, person_id = ? WHERE id = ? ",
                    passport.getNumber(),
                    Timestamp.from(passport.getGivenDate()),
                    passport.getDepartmentCode(),
                    passport.isActive(),
                    passport.getDescription(),
                    passport.getPersonId(),
                    passport.getId());
        } else throw new PassportNotFoundException(passport.getId());
        return passport;
    }

    @Override
    public synchronized Passport deletePassport(String id) {
        Passport passport = findPassportById(id);
        if (passport != null) {
            jdbcTemplate.update("DELETE FROM passportapi1.public.Passport WHERE id = ?;",
                    id);
        } else throw new PassportNotFoundException(id);
        return passport;
    }

    @Override
    public Passport findPassportById(String id) {
        List<Passport> result = jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE id = ?;",
                this::mapToPassport,
                id);
        return result.size() == 0 ? null : result.get(0);
    }

    @Override
    public Passport findPassportById(String id, boolean active) {
        List<Passport> result = jdbcTemplate.query("SELECT*FROM passportapi1.public.Passport WHERE id = ? AND active = ?;",
                this::mapToPassport,
                id, active);
        return result.size() == 0 ? null : result.get(0);
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
                                this::mapToPassport, personId, active, Date.from(startDate), Date.from(endDate)));
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE person_id = ? AND givendate BETWEEN ? AND ?;",
                                this::mapToPassport, personId, Date.from(startDate), Date.from(endDate)));
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
                                this::mapToPassport, active, Date.from(startDate), Date.from(endDate)));
    }

    @Override
    public List<Passport> getPassportsByParams(Instant startDate, Instant endDate) {
        return new ArrayList<>(
                jdbcTemplate.query
                        ("SELECT*FROM passportapi1.public.Passport WHERE givendate BETWEEN ? AND ?;",
                                this::mapToPassport, Date.from(startDate), Date.from(endDate)));
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
        List<Passport> passportList = jdbcTemplate.query
                ("SELECT*FROM passportapi1.public.Passport WHERE number = ?;",
                        this::mapToPassport, number);
        if(passportList.size() == 0) throw new PassportWrongNumberException();
        return passportList.get(0);
    }

    private Passport mapToPassport(ResultSet resultSet, int i) throws SQLException {
        return new Passport(
                resultSet.getString("id"),
                resultSet.getString("person_id"),
                resultSet.getString("number"),
                resultSet.getTimestamp("givenDate").toInstant(),
                resultSet.getString("departmentCode"),
                resultSet.getBoolean("active"),
                resultSet.getString("description")
        );
    }
}
