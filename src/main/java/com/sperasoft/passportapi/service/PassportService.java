package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportDeactivatedException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportNotFoundException;
import com.sperasoft.passportapi.model.LostPassportInfo;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.repository.PassportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportService {

    private final PassportRepository passportRepository;

    public Passport createPassport(Passport passport) {
        return passportRepository.addPassport(passport);
    }

    public Passport findPassportById(String id,
                                     Boolean active) {
        Passport passport = passportRepository.findPassportById(id);
        if (passport == null) throw new PassportNotFoundException(id);
        if (active == null) {
            return passport;
        } else
            return passportRepository.findPassportById(id, active);
    }

    public Passport updatePassport(Passport passport) {
        return passportRepository.updatePassport(passport);
    }

    public Passport deletePassport(String id) {
        return passportRepository.deletePassport(id);
    }

    public List<Passport> getPassportsByPersonIdAndParams(String personId,
                                                          Boolean active,
                                                          @Nullable Instant dateStart,
                                                          @Nullable Instant dateEnd) {
        if (active == null && dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams(personId);
        } else if (dateStart == null && dateEnd == null) {
            return passportRepository.getPassportsByParams(personId, active);
        } else if (dateStart == null || dateEnd == null) {
            if (dateStart == null) {
                dateStart = dateEnd.minusSeconds(360000L);
            } else dateEnd = Instant.now();
        }

        if (dateStart.isAfter(dateEnd)) {
            throw new InvalidPassportDataException();
        }
        if (active == null) {
            return passportRepository.getPassportsByParams(personId, dateStart, dateEnd);
        }
        return passportRepository.getPassportsByParams(personId, active, dateStart, dateEnd);
    }

    public boolean deactivatePassport(String personId,
                                      String id,
                                      LostPassportInfo description) {
        if (description == null) {
            description = new LostPassportInfo("new desc");
        }
        Passport passportPerson =
                passportRepository.findPassportById(id);
        if (passportPerson != null && passportPerson.isActive()) {
            passportRepository.updatePassport(
                    new Passport(passportPerson.getId(),
                            personId,
                            passportPerson.getNumber(),
                            passportPerson.getGivenDate(),
                            passportPerson.getDepartmentCode(),
                            false,
                            description.description()));
            return true;
        } else {
            throw new PassportDeactivatedException();
        }
    }
}
