package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.model.Description;
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
    public boolean deactivatePassport(String personId, String id, boolean active, Description description) {
        if (description == null) {
            description = new Description();
        }
        Passport passportPerson =
                personRepository.findById(personId).getList().stream()
                        .filter(passport ->
                                passport.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Passport not found "));
        if (passportPerson.isActive() == true) {
            passportPerson.setActive(active);
            passportPerson.setDescription(description.getDescription());
            return true;
        } else
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Passport was already deactivated");
    }
}
