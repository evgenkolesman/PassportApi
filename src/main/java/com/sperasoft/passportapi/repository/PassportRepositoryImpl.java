package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.exceptions.passportexceptions.PassportBadStatusException;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class PassportRepositoryImpl implements PassportRepository {

    private final Map<String, Passport> passportRepo = new ConcurrentHashMap<>();

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
    public List<Passport> getPassportsByParams(boolean active, LocalDate dateStart, LocalDate dateEnd) {
        return passportRepo.values().stream().filter(a -> a.isActive() == active)
                .filter(a ->
                        ((dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                (dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate()))))
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(LocalDate dateStart, LocalDate dateEnd) {
        return passportRepo.values().stream()
                .filter(a ->
                        ((dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams(boolean active) {
        return passportRepo.values().stream()
                .filter(a -> a.isActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public List<Passport> getPassportsByParams() {
        return new ArrayList<>(passportRepo.values());
    }
}
