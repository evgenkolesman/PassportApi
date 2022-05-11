package com.sperasoft.passportapi.service;

import com.sperasoft.passportapi.dto.PassportResponse;
import com.sperasoft.passportapi.dto.PersonResponse;
import com.sperasoft.passportapi.repository.PassportRepository;
import com.sperasoft.passportapi.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final PassportRepository passportRepository;
    private final PersonRepository personRepository;

    @Override
    public PersonResponse findPersonByPassportNumber(String number) {
        return passportRepository.getPassportsByParams().stream()
                .filter(passport -> passport.getNumber().equals(number))
                .map(person -> personRepository.findAll()
                        .stream()
                        .filter(person1 ->
                                person1.getList().stream().anyMatch(
                                        p -> p.getNumber().equals(number))).findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "No such number")))
                .map(PersonResponse::of)
                .findFirst().get();
    }

    @Override
    public List<PassportResponse> getAllPassports(String active,
                                                  String dateStart, String dateEnd) throws ParseException {
        if (active.isEmpty() && dateStart.isEmpty() && dateEnd.isEmpty()) {
            return passportRepository.getPassportsByParams().stream()
                    .map(PassportResponse::of)
                    .collect(Collectors.toList());
        } else if (dateStart.isEmpty() && dateEnd.isEmpty()) {
            return passportRepository.getPassportsByParams(Boolean.parseBoolean(active)).stream()
                    .map(PassportResponse::of)
                    .collect(Collectors.toList());
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateFirst = format.parse(dateStart);
        Date dateSecond = format.parse(dateEnd);
        if (dateFirst.after(dateSecond)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid data period: Start date is after End date");
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
