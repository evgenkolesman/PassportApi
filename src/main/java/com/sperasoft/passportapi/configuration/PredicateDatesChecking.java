package com.sperasoft.passportapi.configuration;

import com.sperasoft.passportapi.model.Passport;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * All config was replaced to common config
 */

//@Configuration
@Deprecated
public class PredicateDatesChecking {


    public BiPredicate<Passport, List<Instant>> getPassportListBiPredicate() {
        return (passport, timeList) ->
                (timeList.get(0).isBefore(passport.getGivenDate())) &&
                timeList.get(1).isAfter(passport.getGivenDate());
    }
}
