package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final PassportRepositoryImpl passportRepository;
    private final PersonRepositoryImpl personRepositoryImpl;


    public Person findPersonByPassportNumber(String number) {
        return personRepositoryImpl.findAll().stream().map(person -> personRepositoryImpl.findAll()
                        .stream()
                        .filter(person1 ->
                                person1.getList().stream().anyMatch(
                                        p -> p.getNumber().equals(number))).findFirst()
                        .orElseThrow(() -> {
                            throw new PassportWrongNumberException();
                        }))
                .findFirst().get();
    }

    public List<Passport> getAllPassports(Boolean active,
                                          ZonedDateTime dateStart, ZonedDateTime dateEnd) {
        if (active == null && dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams();
        } else if (dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams(active);
        } else if (dateStart == null || dateEnd == null) {
            if (dateStart == null) {
                dateStart = dateEnd;
            } else dateEnd = ZonedDateTime.now();
        }
        if (dateStart.isAfter(dateEnd)) {
            throw new InvalidPassportDataException();
        }
        if (active == null) {
            return passportRepository.getPassportsByParams(dateStart, dateEnd);
        }
        return passportRepository.getPassportsByParams(active, dateStart, dateEnd);
    }
}
