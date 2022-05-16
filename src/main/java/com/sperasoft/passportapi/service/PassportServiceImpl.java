package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        if (passportRepository.isPassportPresent(passportRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data");
        }
        Person person = personRepository.findPersonById(personId);
        return PassportResponse.of(passportRepository.addPassport(passportRequest, person));
    }

    @Override
    public PassportResponse findPassportById(String personId, String id, String active) {
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid passport ID");
        }
        if (active.isEmpty()) {
            return PassportResponse.of(passportRepository.findPassportById(id));
        } else
            return PassportResponse.of(passportRepository.findPassportById(id, Boolean.parseBoolean(active)));
    }

    @Override
    public PassportResponse updatePassport(String personId, String id, PassportRequest passport) {
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passport not found");
        }
        if (passportRepository.isPassportPresent(passport)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Such passport presents cannot be added");
        }
        return PassportResponse.of(passportRepository.updatePassport(id, passport));
    }

    @Override
    public PassportResponse deletePassport(String personId, String id) {
        if (passportRepository.findPassportById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Passport not found");
        }
        return PassportResponse.of(passportRepository.deletePassport(id));
    }

    @Override
    public List<PassportResponse> getPassportsByPersonIdAndParams(String personId, String active,
                                                                  String dateStart, String dateEnd) {
        Person person = personRepository.findPersonById(personId);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid data period: Start date is after End date");
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

}
