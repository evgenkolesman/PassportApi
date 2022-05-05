package com.sperasoft.passportapi.dto;

import com.sperasoft.passportapi.model.Person;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Date;
import java.util.List;

@Data
public class PersonResponse {

    String id;

    String name;

    Date birthday;

    String birthdayCountry;

    public static PersonResponse of(Person personStore) {
        ModelMapper model = new ModelMapper();
        model.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        PersonResponse personResponse =  model.map(personStore, PersonResponse.class);
        return personResponse;
    }
}
