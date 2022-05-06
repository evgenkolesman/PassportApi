package com.sperasoft.passportapi.controller;

import com.sperasoft.passportapi.model.Description;
import com.sperasoft.passportapi.service.LostPassportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("person/{personId}/passport/{id}/lostPassport")
@RequiredArgsConstructor
public class LostPassportController {

    private final LostPassportService service;

    @PostMapping
    public boolean lostPassportDeactivate(@PathVariable String personId,
                                          @PathVariable String id,
                                          @RequestParam boolean active,
                                          @RequestBody(required = false) Description description) {
        return service.deactivatePassport(personId, id, active, description);
    }
}
