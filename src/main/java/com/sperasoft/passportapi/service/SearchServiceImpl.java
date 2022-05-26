package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.controller.dto.PersonResponse;
import com.sperasoft.passportapi.exceptions.passportexceptions.InvalidPassportDataException;
import com.sperasoft.passportapi.exceptions.passportexceptions.PassportWrongNumberException;
import com.sperasoft.passportapi.repository.PassportRepositoryImpl;
import com.sperasoft.passportapi.repository.PersonRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl {

    private final PassportRepositoryImpl passportRepository;
    private final PersonRepositoryImpl personRepositoryImpl;


    public PersonResponse findPersonByPassportNumber(String number) {
        return personRepositoryImpl.findAll().stream().map(person -> personRepositoryImpl.findAll()
                        .stream()
                        .filter(person1 ->
                                person1.getList().stream().anyMatch(
                                        p -> p.getNumber().equals(number))).findFirst()
                        .orElseThrow(() -> {
                            throw new PassportWrongNumberException();
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
            throw new InvalidPassportDataException();
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
