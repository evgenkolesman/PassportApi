package com.sperasoft.passportapi;

import com.sperasoft.passportapi.controller.dto.PassportResponse;
import com.sperasoft.passportapi.model.Passport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Configuration
public class ConfigurationTest {
    private static final String HTTP_LOCALHOST = "http://localhost";

    @Bean
    public UriComponentsBuilder getUriComponentsBuilder() {
        return UriComponentsBuilder
                .fromHttpUrl(HTTP_LOCALHOST);
    }

    @Bean(name = "predicate")
    public BiPredicate<Passport, Passport> predicate(){
        return (passport, passport1) -> {
            return passport.getNumber().equals(passport1.getNumber()) &&
                    passport.getId().equals(passport1.getId()) &&
                    passport.getPersonId().equals(passport1.getPersonId()) &&
                    passport.getDepartmentCode().equals(passport1.getDepartmentCode()) &&
                    passport.isActive() == passport1.isActive() &&
                    passport.getGivenDate().compareTo(passport1.getGivenDate()) >= 0;
        };
    }

    @Bean(name = "predicatePassportResponse")
    public BiPredicate<PassportResponse, PassportResponse> predicatePassportResponse(){
        return (passport, passport1) -> {
            return passport.getNumber().equals(passport1.getNumber()) &&
                    passport.getId().equals(passport1.getId()) &&
                    passport.getDepartmentCode().equals(passport1.getDepartmentCode()) &&
                    passport.getGivenDate().compareTo(passport1.getGivenDate()) >= 0;
        };
    }

    @Bean(name = "listPredicate")
    public BiPredicate<List<Passport>, List <Passport>> listPredicate(BiPredicate<Passport, Passport> predicate) {
        return (list1, list2) -> {
            assertTrue(list1.size() == list2.size());
            boolean result = false;
            for(int i = 0; i < list1.size(); i++) {
                result = predicate.test(list1.get(i), list2.get(i));
            }
            return result;
        };
    }

    @Bean(name = "listPredicatePassportResponse")
    public BiPredicate<List<PassportResponse>,List <PassportResponse>> listPredicatePassportResponse(
            BiPredicate<PassportResponse, PassportResponse> predicatePassportResponse) {
        return (list1, list2) -> {
            assertTrue(list1.size() == list2.size());
            boolean result = false;
            for(int i = 0; i < list1.size(); i++) {
                result = predicatePassportResponse.test(list1.get(i), list2.get(i));
            }
            return result;
        };
    }
}
