package com.sperasoft.passportapi.controller.abstracts;

import com.sperasoft.passportapi.TestContainersInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("Test")
@Transactional
@Slf4j
@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        TestContainersInitializer.Initializer.class
})

public class TestAbstractIntegration {
}
