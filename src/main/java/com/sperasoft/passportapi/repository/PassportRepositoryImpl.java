package com.sperasoft.passportapi.repository;

import com.google.common.collect.Range;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportBadStatusException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
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
public class PassportRepositoryImpl implements PassportRepository {

//    private final BiPredicate<Passport, List<Instant>> predicateDatesChecking;
    private static final Map<String, Passport> passportRepo = new ConcurrentHashMap<>();

    @Override
    public Passport addPassport(Passport passport, Person person) {
        person.getList().add(passport);
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
            return passportRepo.get(id);
        } else
            throw new PassportBadStatusException();
    }

    @Override
    public Passport updatePassport(Passport passport) {
        passportRepo.replace(passport.getId(), passport);
        return passport;
    }

    @Override
    public Passport deletePassport(String id) {
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
    public List<Passport> getPassportsByParams() {
        return new ArrayList<>(passportRepo.values());
    }

}
