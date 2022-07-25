package com.sperasoft.passportapi.repository;

import com.google.common.collect.Range;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportBadStatusException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportNotFoundException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWasAddedException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.exceptions.personexceptions.PersonNotFoundException;
import com.sperasoft.passportapi.model.Passport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//@Primary
public class PassportRepositoryImpl implements PassportRepository {

    private static final Map<String, Passport> passportRepo = new ConcurrentHashMap<>();

    private final PersonRepository personRepository;

    @Override
    public synchronized Passport addPassport(Passport passport) {
        if (personRepository.findById(passport.getPersonId()) == null) {
            throw new PersonNotFoundException(passport.getId());
        }
        if (passportRepo.values().stream().anyMatch(a ->
            (a.getNumber().equals(passport.getNumber())))) throw new PassportWasAddedException();
        passportRepo.put(passport.getId(), passport);
        return passport;
    }

    @Override
    public Passport findPassportById(String id) {
        return passportRepo.get(id);
    }

    @Override
    public Passport findPassportById(String id, boolean active) {
        Passport passport = passportRepo.get(id);
        if (passport.isActive() == active) {
            return passport;
        } else
            throw new PassportBadStatusException();
    }

    @Override
    public synchronized Passport updatePassport(Passport passport) {
        if (personRepository.findById(passport.getPersonId()) == null) {
            throw new PersonNotFoundException(passport.getId());
        }
        checkPassportPresentWithId(passport.getId());
        passportRepo.replace(passport.getId(), passport);
        return passport;
    }

    @Override
    public synchronized Passport deletePassport(String id) {
        checkPassportPresentWithId(id);
        return passportRepo.remove(id);
    }

    @Override
    public List<Passport> getPassportsByParams(Boolean active, Instant dateStart, Instant dateEnd) {
        Range<Instant> dateRange = Range.closed(dateStart, dateEnd);
        return passportRepo.values().stream().filter(a -> a.isActive() == active)
                .filter(passport ->
                        dateRange.test(passport.getGivenDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(Instant dateStart, Instant dateEnd) {
        Range<Instant> dateRange = Range.closed(dateStart, dateEnd);
        return passportRepo.values().stream()
                .filter(passport ->
                        dateRange.test(passport.getGivenDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(Boolean active) {
        return passportRepo.values().stream()
                .filter(a -> a.isActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public Passport getPassportByNumber(String number) {
        return passportRepo.values().stream()
                .filter(passport -> passport.getNumber().equals(number))
                .findFirst()
                .orElseThrow(PassportWrongNumberException::new);
    }

    @Override
    public List<Passport> getPassportsByParams() {
        return new ArrayList<>(passportRepo.values());
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Boolean active, Instant startDate, Instant endDate) {
        if (personRepository.findById(personId) == null) {
            throw new PersonNotFoundException(personId);
        }
        Range<Instant> dateRange = Range.closed(startDate, endDate);
        return passportRepo.values().stream()
                .filter(passportFromRepo -> passportFromRepo.getPersonId().equals(personId))
                .filter(a -> a.isActive() == active)
                .filter(passport ->
                        dateRange.test(passport.getGivenDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(String personId, Instant startDate, Instant endDate) {
        if (personRepository.findById(personId) == null) {
            throw new PersonNotFoundException(personId);
        }
        Range<Instant> dateRange = Range.closed(startDate, endDate);
        return passportRepo.values().stream()
                .filter(passportFromRepo -> passportFromRepo.getPersonId().equals(personId))
                .filter(passport ->
                        dateRange.test(passport.getGivenDate()))
                .collect(Collectors.toList());
    }


    @Override
    public List<Passport> getPassportsByParams(String personId, Boolean active) {
        if (personRepository.findById(personId) == null) {
            throw new PersonNotFoundException(personId);
        }
        return passportRepo.values().stream()
                .filter(passportFromRepo -> passportFromRepo.getPersonId().equals(personId))
                .filter(a -> a.isActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(String personId) {
        if (personRepository.findById(personId) == null) {
            throw new PersonNotFoundException(personId);
        }
        return passportRepo.values().stream()
                .filter(passportFromRepo -> passportFromRepo.getPersonId().equals(personId))
                .collect(Collectors.toList());
    }

    private void checkPassportPresentWithId(String id) {
        if (!passportRepo.containsKey(id)) {
            throw new PassportNotFoundException(id);
        }
    }
}
