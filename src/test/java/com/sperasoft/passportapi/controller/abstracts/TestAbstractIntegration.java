package com.sperasoft.passportapi.controller.abstracts;


import com.sperasoft.passportapi.TestContainersInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest(webEnvironment =
        SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {
        TestContainersInitializer.Initializer.class
})

public class TestAbstractIntegration {

    @BeforeAll
    static void init() {
        TestContainersInitializer.container.start();
    }
}
