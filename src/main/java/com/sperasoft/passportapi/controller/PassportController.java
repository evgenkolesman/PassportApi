package com.sperasoft.passportapi.controller;

import com.devskiller.friendly_id.FriendlyId;
import com.sperasoft.passportapi.controller.dto.PassportRequest;
import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.LostPassportInfo;
import com.sperasoft.passportapi.model.Passport;
import com.sperasoft.passportapi.service.PassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/person/{personId}/passport")
@RequiredArgsConstructor
public class PassportController {
    private final PassportService passportService;

    @GetMapping
    public List<PassportResponse> findPersonPassports(@PathVariable(name = "personId") String personId,
                                                      @RequestParam(name = "active")
                                                      @Nullable Boolean active,
                                                      @RequestParam(name = "dateStart")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                      @Nullable Instant dateStart,
                                                      @RequestParam(name = "dateEnd")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                      @Nullable Instant dateEnd) {
        return passportService.getPassportsByPersonIdAndParams(personId, active, dateStart, dateEnd)
                .stream().map(PassportResponse::of)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PassportResponse createPassport(@PathVariable("personId") String personId,
                                           @RequestBody @Valid PassportRequest passportRequest) {
        return PassportResponse.of(passportService.createPassport(
                new Passport(FriendlyId.createFriendlyId(),
                        personId,
                        passportRequest.getNumber(),
                        passportRequest.getGivenDate(),
                        passportRequest.getDepartmentCode())));
    }

    @GetMapping("/{id}")
    public PassportResponse findPassport(@PathVariable("id") String id,
                                         @RequestParam(name = "active")
                                         @Nullable Boolean active) {
        return PassportResponse.of(passportService.findPassportById(id, active));
    }

    @PutMapping("/{id}")
    public PassportResponse updatePassport(@PathVariable("personId") String personId,
                                           @PathVariable("id") String id,
                                           @RequestBody @Valid PassportRequest passportRequest) {
        return PassportResponse.of(passportService.updatePassport(
                new Passport(id,
                personId,
                passportRequest.getNumber(),
                passportRequest.getGivenDate(),
                passportRequest.getDepartmentCode())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePassport(@PathVariable("id") String id) {
        passportService.deletePassport(id);
    }

    @PostMapping("/{id}/lostPassport")
    public boolean lostPassportDeactivate(@PathVariable("personId") String personId,
                                          @PathVariable("id") String id,
                                          @RequestBody(required = false) LostPassportInfo description) {
        return passportService.deactivatePassport(personId, id, description);
    }
}
