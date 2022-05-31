package com.sperasoft.passportapi.configuration;

import com.sperasoft.passportapi.model.Passport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.function.BiPredicate;

@Configuration
@ComponentScan(lazyInit = true)
public class PredicateDatesChecking {

    @Bean
    public BiPredicate<Passport, List<ZonedDateTime>> getPassportListBiPredicate() {
        return (passport, timeList) ->
                (timeList.get(0).isBefore(ChronoZonedDateTime.from(
                        passport.getGivenDate().atStartOfDay().atZone(timeList.get(0).getZone())))
                        || timeList.get(0).isEqual(ChronoZonedDateTime.from(
                        passport.getGivenDate().atStartOfDay().atZone(timeList.get(0).getZone()))))
                        && (timeList.get(1).isAfter(ChronoZonedDateTime.from(
                        passport.getGivenDate().atStartOfDay().atZone(timeList.get(1).getZone())))
                        || timeList.get(1).isEqual(ChronoZonedDateTime.from(
                        passport.getGivenDate().atStartOfDay().atZone(timeList.get(1).getZone()))));
    }

    public boolean test(Passport passport, List<ZonedDateTime> listDates) {
        return getPassportListBiPredicate().test(passport, listDates);
    }
}
