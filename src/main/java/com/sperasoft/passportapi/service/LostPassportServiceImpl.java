package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LostPassportServiceImpl implements LostPassportService {

    private final PersonRepository personRepository;

    @Override
    public boolean deactivatePassport(String personId, String id, boolean active) {
        if (personRepository.findById(personId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID");
        }
        Passport passportPerson =
                personRepository.findById(personId).getList().stream()
                        .filter(passport -> passport.getId().equals(id)).findFirst().orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid person ID"));
        if (passportPerson.isActive() != active) {
            passportPerson.setActive(active);
            return true;
        } else
            return false;
    }
}
