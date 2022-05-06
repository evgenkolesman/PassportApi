package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PassportRequest;
import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportServiceImpl implements PassportService {

    private final PassportRepository passportRepository;
    private final PersonRepository personRepository;


    @Override
    public PassportResponse addPassportToPerson(String personId, PassportRequest passportRequest) {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        if (passportRepository.isPassportPresent(passportRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such passport presents cannot be added");
        }
        Person person = personRepository.findPersonById(personId);
        return PassportResponse.of(passportRepository.addPassport(passportRequest, person));
    }

    @Override
    public PassportResponse findPassportById(String personId, String id, String active) {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid person ID");
        }
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid person ID");
        }
        if (active.isEmpty()) {
            return PassportResponse.of(passportRepository.findPassportById(id));
        } else
            return PassportResponse.of(passportRepository.findPassportById(id, Boolean.valueOf(active)));
    }

    @Override
    public PassportResponse updatePassport(String personId, String id, PassportRequest passport) {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        if (passportRepository.isPassportPresent(passport)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such passport presents cannot be added");
        }
        return PassportResponse.of(passportRepository.updatePassport(id, passport));
    }

    @Override
    public PassportResponse deletePassport(String personId, String id) {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        Person person = personRepository.findPersonById(personId);
        person.getList().stream().filter(a -> {
            if (a.getId().equals(id)) {
                log.info(a.toString());
            }
            return !a.getId().equals(id);
        }).collect(Collectors.toList());
        return PassportResponse.of(passportRepository.deletePassport(id));
    }

    @Override
    public List<PassportResponse> getPassportsByPersonIdAndParams(String personId, String active,
                                                                  String dateStart, String dateEnd) throws ParseException {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid person ID");
        }
        Person person = personRepository.findPersonById(personId);
        if (active.isEmpty() && dateStart.isEmpty() && dateEnd.isEmpty()) {
            return person.getList().stream().map(PassportResponse::of).collect(Collectors.toList());
        } else if (dateStart.isEmpty() && dateEnd.isEmpty()) {
            return getPassportsByPersonAndParams(person, Boolean.valueOf(active));
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFirst = format.parse(dateStart);
        Date dateSecond = format.parse(dateEnd);
        if (dateFirst.after(dateSecond)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid data period: Start date is after End date");
        }
        if (active.isEmpty()) {
            return getPassportsByPersonAndParams(person, dateFirst, dateSecond);
        }
        return getPassportsByPersonAndParams(person, Boolean.valueOf(active), dateFirst, dateSecond);
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 Date dateStart,
                                                                 Date dateEnd) {
        if (dateStart.after(dateEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid data period: Start date is after End date");
        }
        return person.getList().stream().filter(a ->
                        (dateStart.before(a.getGivenDate()) || dateStart.equals(a.getGivenDate()) &&
                                (dateEnd.after(a.getGivenDate()) || dateEnd.equals(a.getGivenDate()))))
                .map(PassportResponse::of).collect(Collectors.toList());
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 boolean active,
                                                                 Date dateStart,
                                                                 Date dateEnd) {
        if (dateStart.after(dateEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid data period: Start date is after End date");
        }
        return person.getList().stream().filter(a -> a.isActive() == active).filter(a ->
                        (dateStart.before(a.getGivenDate()) || dateStart.equals(a.getGivenDate()) &&
                                (dateEnd.after(a.getGivenDate()) || dateEnd.equals(a.getGivenDate()))))
                .map(PassportResponse::of).collect(Collectors.toList());
    }

    private List<PassportResponse> getPassportsByPersonAndParams(Person person,
                                                                 boolean active) {
        return person.getList().stream().filter(a -> a.isActive() == active)
                .map(PassportResponse::of).collect(Collectors.toList());
    }

}
