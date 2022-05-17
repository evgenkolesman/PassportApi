package com.sperasoft.passportapi.repository;

import com.sperasoft.passportapi.ModelMapperMaker;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.model.Person;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//TODO add repository interfaces

@Repository
public class PassportRepository {

    private final Map<String, Passport> passportRepo = new ConcurrentHashMap<>();

    public boolean isPassportPresent(PassportRequest passportRequest) {
        return passportRepo.values().stream().anyMatch(p -> {
            PassportRequest pr = ModelMapperMaker.configMapper().map(p, PassportRequest.class);
            return pr.equals(passportRequest);
        });
    }

    public Passport addPassport(PassportRequest passportRequest, Person person) {
        Passport passport = Passport.of(passportRequest);
        person.getList().add(passport);
        passportRepo.put(passport.getId(), passport);
        return passport;
    }

    public Passport findPassportById(String id) {
        return passportRepo.get(id);
    }

    public Passport findPassportById(String id, boolean active) {
        Passport passport = passportRepo.get(id);
        if (passport.isActive() == active) {
            return passportRepo.get(id);
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passport has another status");
    }

    public Passport updatePassport(String id, PassportRequest passportRequest) {
        Passport passport = passportRepo.get(id);
        passport.setNumber(passportRequest.getNumber());
        passport.setGivenDate(passportRequest.getGivenDate());
        passport.setDepartmentCode(passportRequest.getDepartmentCode());
        passportRepo.replace(id, passport);
        return passport;
    }

    public Passport deletePassport(String id) {
        return passportRepo.remove(id);
    }

    public List<Passport> getPassportsByParams(boolean active, LocalDate dateStart, LocalDate dateEnd) {
        return passportRepo.values().stream().filter(a -> a.isActive() == active)
                .filter(a ->
                        ((dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                (dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate()))))
                .collect(Collectors.toList());
    }

    public List<Passport> getPassportsByParams(LocalDate dateStart, LocalDate dateEnd) {
        return passportRepo.values().stream()
                .filter(a ->
                        ((dateStart.isBefore(a.getGivenDate()) || dateStart.isEqual(a.getGivenDate())) &&
                                dateEnd.isAfter(a.getGivenDate()) || dateEnd.isEqual(a.getGivenDate())))
                .collect(Collectors.toList());
    }

    public List<Passport> getPassportsByParams(boolean active) {
        return passportRepo.values().stream()
                .filter(a -> a.isActive() == active)
                .collect(Collectors.toList());
    }

    public List<Passport> getPassportsByParams() {
        return new ArrayList<>(passportRepo.values());
    }
}
