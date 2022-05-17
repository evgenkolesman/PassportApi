package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl {

    private final PassportRepositoryImpl passportRepository;
    private final PersonRepositoryImpl personRepositoryImpl;
    private final Environment environment;

    public PersonResponse findPersonByPassportNumber(String number) {
        return passportRepository.getPassportsByParams().stream()
                .filter(passport -> passport.getNumber().equals(number))
                .map(person -> personRepositoryImpl.findAll()
                        .stream()
                        .filter(person1 ->
                                person1.getList().stream().anyMatch(
                                        p -> p.getNumber().equals(number))).findFirst()
                        .orElseThrow(() -> {
                            log.info(String.format("%s %s %s", UUID.randomUUID(),
                                    HttpStatus.BAD_REQUEST,
                                    Objects.requireNonNull(environment.getProperty("searches.exception.wrong-num"))));
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    environment.getProperty("searches.exception.wrong-num"));
                        }))
                .map(PersonResponse::of)
                .findFirst().get();
    }

    public List<PassportResponse> getAllPassports(String active,
                                                  String dateStart, String dateEnd) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (active.isEmpty() && dateStart.isEmpty() && dateEnd.isEmpty()) {
            return passportRepository.getPassportsByParams().stream()
                    .map(PassportResponse::of)
                    .collect(Collectors.toList());
        } else if (dateStart.isEmpty() && dateEnd.isEmpty()) {
            return passportRepository.getPassportsByParams(Boolean.parseBoolean(active)).stream()
                    .map(PassportResponse::of)
                    .collect(Collectors.toList());
        } else if (dateStart.isEmpty() || dateEnd.isEmpty()) {
            if (dateStart.isEmpty()) {
                dateStart = dateEnd;
            } else dateEnd = format.format(LocalDate.now());
        }
        LocalDate dateFirst = LocalDate.parse(dateStart, format);
        LocalDate dateSecond = LocalDate.parse(dateEnd, format);
        if (dateFirst.isAfter(dateSecond)) {
            log.info(String.format("%s %s %s", UUID.randomUUID(),
                    HttpStatus.BAD_REQUEST,
                    Objects.requireNonNull(environment.getProperty("passport.exception.invalid.date"))));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    environment.getProperty("passport.exception.invalid.date"));
        }
        if (active.isEmpty()) {
            return passportRepository.getPassportsByParams(dateFirst, dateSecond).stream()
                    .map(PassportResponse::of)
                    .collect(Collectors.toList());
        }

        return passportRepository.getPassportsByParams(Boolean.parseBoolean(active), dateFirst, dateSecond).stream()
                .map(PassportResponse::of)
                .collect(Collectors.toList());
    }
}
