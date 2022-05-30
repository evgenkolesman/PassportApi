package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.exceptions.passportexceptions.*;
import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportService {

    private final PassportRepositoryImpl passportRepository;
    private final PersonRepositoryImpl personRepositoryImpl;

    public Passport addPassportToPerson(String personId, Passport passport) {
        if (personRepositoryImpl.findPersonById(personId).getList().stream().anyMatch(p ->
                p.getNumber().equals(passport.getNumber()))) {
            throw new PassportWasAddedException();
        }
        Person person = personRepositoryImpl.findPersonById(personId);
        return passportRepository.addPassport(passport, person);
    }

    public Passport findPassportById(String id, Boolean active) {
        checkPassportPresentWithId(id);
        if (active == null) {
            return passportRepository.findPassportById(id);
        } else
            return passportRepository.findPassportById(id, active);
    }

    public Passport updatePassport(String id, Passport passport) {
        checkPassportPresentWithId(id);
        passport.setId(id);
        return passportRepository.updatePassport(passport);
    }

    public Passport deletePassport(String id) {
        checkPassportPresentWithId(id);
        return passportRepository.deletePassport(id);
    }

    public List<Passport> getPassportsByPersonIdAndParams(String personId, Boolean active,
                                                          ZonedDateTime dateStart, ZonedDateTime dateEnd) {
        Person person = personRepositoryImpl.findPersonById(personId);
        if (person.getList().size() == 0) {
            throw new PassportEmptyException(personId);
        }
        if (active == null && dateStart == null && dateEnd == null) {
            return new ArrayList<>(person.getList());
        } else if (dateStart == null && dateEnd == null) {
            return getPassportsByPersonAndParams(person, active);
        } else if (dateStart == null || dateEnd == null) {
            if (dateStart == null) {
                dateStart = dateEnd;
            } else dateEnd = ZonedDateTime.now();
        }

        if (dateStart.isAfter(dateEnd)) {
            throw new InvalidPassportDataException();
        }
        if (active == null) {
            return getPassportsByPersonAndParams(person, dateStart, dateEnd);
        }
        return getPassportsByPersonAndParams(person, active, dateStart, dateEnd);
    }

    private List<Passport> getPassportsByPersonAndParams(Person person,
                                                         ZonedDateTime dateStart,
                                                         ZonedDateTime dateEnd) {
        return person.getList().stream().filter(a ->
                        (dateStart.isBefore(ChronoZonedDateTime.from(a.getGivenDate().atZone(ZoneId.systemDefault())))
                                || dateStart.isEqual(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault())))
                                && (dateEnd.isAfter(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault())))
                                || dateEnd.isEqual(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault())))
                .collect(Collectors.toList());
    }

    private List<Passport> getPassportsByPersonAndParams(Person person,
                                                         boolean active,
                                                         ZonedDateTime dateStart,
                                                         ZonedDateTime dateEnd) {
        return person.getList().stream().filter(a -> a.isActive() == active).filter(a ->
                        (dateStart.isBefore(ChronoZonedDateTime.from(a.getGivenDate().atZone(ZoneId.systemDefault())))
                                || dateStart.isEqual(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault())))
                                && (dateEnd.isAfter(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault()))
                                || dateEnd.isEqual(ChronoZonedDateTime.from(a.getGivenDate()).toLocalDateTime().atZone(ZoneId.systemDefault()))))
                .collect(Collectors.toList());
    }

    private List<Passport> getPassportsByPersonAndParams(Person person,
                                                         boolean active) {
        return person.getList().stream()
                .filter(a -> a.isActive() == active)
                .collect(Collectors.toList());
    }

    public boolean deactivatePassport(String personId, String id,
                                      Boolean active,
                                      Description description) {
        if (description == null) {
            description = new Description();
        }
        Passport passportPerson =
                personRepositoryImpl.findById(personId).getList().stream()
                        .filter(passport ->
                                passport.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> {
                            throw new PassportNotFoundException(id);
                        });
        if (passportPerson.isActive() == true) {
            passportPerson.setActive(active);
            passportPerson.setDescription(description.getDescription());
            return true;
        } else {
            throw new PassportDeactivatedException();
        }
    }

    private void checkPassportPresentWithId(String id) {
        if (passportRepository.findPassportById(id) == null) {
            throw new PassportNotFoundException(id);
        }
    }

}
