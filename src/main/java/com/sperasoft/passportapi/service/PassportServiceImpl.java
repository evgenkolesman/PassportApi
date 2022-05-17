package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.configuration.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportServiceImpl {

    private final PassportRepositoryImpl passportRepository;
    private final PersonRepositoryImpl personRepositoryImpl;
    private final Environment environment;

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public boolean isPassportPresent(PassportRequest passportRequest) {
        return passportRepository.getPassportsByParams().stream().anyMatch(p -> {
            PassportRequest pr = ModelMapperMaker.configMapper().map(p, PassportRequest.class);
            return pr.equals(passportRequest);
        });
    }

    public PassportResponse addPassportToPerson(String personId, PassportRequest passportRequest) {
        if (isPassportPresent(passportRequest)) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.BAD_REQUEST, environment.getProperty("passport.exception.was-added")));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    environment.getProperty("passport.exception.was-added"));
        }
        Person person = personRepositoryImpl.findPersonById(personId);
        Passport passport = Passport.of(passportRequest);
        return PassportResponse.of(passportRepository.addPassport(passport, person));
    }

    public PassportResponse findPassportById(String id, String active) {
        checkPassportPresentWithId(id);
        if (active.isEmpty()) {
            return PassportResponse.of(passportRepository.findPassportById(id));
        } else
            return PassportResponse.of(passportRepository.findPassportById(id, Boolean.parseBoolean(active)));
    }

    private void checkPassportPresentWithId(String id) {
        if (passportRepository.findPassportById(id) == null) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(environment.getProperty("passport.exception.notfound")), id)));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(environment.getProperty("passport.exception.notfound")), id));
        }
    }

    public PassportResponse updatePassport(String id, PassportRequest passportRequest) {
        checkPassportPresentWithId(id);
        Passport passport = Passport.of(passportRequest);
        passport.setId(id);
        return PassportResponse.of(passportRepository.updatePassport(passport));
    }

    public PassportResponse deletePassport(String id) {
        checkPassportPresentWithId(id);
        return PassportResponse.of(passportRepository.deletePassport(id));
    }

    public List<PassportResponse> getPassportsByPersonIdAndParams(String personId, String active,
                                                                  String dateStart, String dateEnd) {
        Person person = personRepositoryImpl.findPersonById(personId);
        if (person.getList().size() == 0) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(
                            environment.getProperty("passport.exception.person.nopassport")), personId)));
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format(Objects.requireNonNull(
                            environment.getProperty("passport.exception.person.nopassport")), personId));
        }
        if (active.isEmpty() && dateStart.isEmpty() && dateEnd.isEmpty()) {
            return person.getList().stream().map(PassportResponse::of).collect(Collectors.toList());
        } else if (dateStart.isEmpty() && dateEnd.isEmpty()) {
            return getPassportsByPersonAndParams(person, Boolean.parseBoolean(active));
        } else if (dateStart.isEmpty() || dateEnd.isEmpty()) {
            if (dateStart.isEmpty()) {
                dateStart = dateEnd;
            } else dateEnd = LocalDate.now().format(format);
        }
        LocalDate dateFirst = LocalDate.parse(dateStart, format);
        LocalDate dateSecond = LocalDate.parse(dateEnd);
        if (dateFirst.isAfter(dateSecond)) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.BAD_REQUEST,
                    Objects.requireNonNull(
                            environment.getProperty("passport.exception.invalid.date"))));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    environment.getProperty("passport.exception.invalid.date"));
        }
        if (active.isEmpty()) {
            return getPassportsByPersonAndParams(person, dateFirst, dateSecond);
        }
        return getPassportsByPersonAndParams(person, Boolean.parseBoolean(active), dateFirst, dateSecond);
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 LocalDate dateStart,
                                                                 LocalDate dateEnd) {
        return person.getList().stream().filter(a ->
                        (dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                (dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate())))
                .map(PassportResponse::of).collect(Collectors.toList());
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 boolean active,
                                                                 LocalDate dateStart,
                                                                 LocalDate dateEnd) {
        return person.getList().stream().filter(a -> a.isActive() == active).filter(a ->
                        (dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                (dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate())))
                .map(PassportResponse::of).collect(Collectors.toList());
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 boolean active) {
        return person.getList().stream().filter(a -> a.isActive() == active)
                .map(PassportResponse::of).collect(Collectors.toList());
    }

    public boolean deactivatePassport(String personId, String id, boolean active, Description description) {
        if (description == null) {
            description = new Description();
        }
        Passport passportPerson =
                personRepositoryImpl.findById(personId).getList().stream()
                        .filter(passport ->
                                passport.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> {
                            log.info(String.format("%s %s %s", UUID.randomUUID(),
                                    HttpStatus.NOT_FOUND,
                                    Objects.requireNonNull(
                                            String.format(Objects.requireNonNull(
                                                    environment.getProperty("passport.exception.notfound")), id))));
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    String.format(Objects.requireNonNull(
                                            environment.getProperty("passport.exception.notfound")), id));

                        });
        if (passportPerson.isActive() == true) {
            passportPerson.setActive(active);
            passportPerson.setDescription(description.getDescription());
            return true;
        } else
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.CONFLICT,
                    Objects.requireNonNull(
                            environment.getProperty("passport.exception.deactivated"))));
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    environment.getProperty("passport.exception.deactivated"));
    }

}
