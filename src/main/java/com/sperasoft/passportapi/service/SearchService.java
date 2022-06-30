package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final PassportRepository passportRepository;
    private final PersonRepository personRepository;


    public Person findPersonByPassportNumber(String number) {
        Passport passport = passportRepository.getPassportByNumber(number);
        return personRepository.findById(passport.getPersonId());
    }

    public List<Passport> getAllPassports(Boolean active,
                                          Instant dateStart,
                                          Instant dateEnd) {
        if (active == null && dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams();
        } else if (dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams(active);
        } else if (dateStart == null || dateEnd == null) {
            if (dateStart == null) {
                dateStart = dateEnd;
            } else dateEnd = Instant.now();
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
