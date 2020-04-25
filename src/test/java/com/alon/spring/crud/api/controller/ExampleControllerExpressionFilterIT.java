package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.cleaner.DatabaseCleaner;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static com.alon.spring.crud.api.controller.ExampleCreator.insertBatchOfExamples;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:filter-by-expression-test.properties")
public class ExampleControllerExpressionFilterIT {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Before
    public void init() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        databaseCleaner.clearTables();
    }

    @Test
    public void whenGetFilteredByExpressionThenReturn() {
        insertBatchOfExamples(3);

        given()
                .queryParam("filter", "stringProperty:Example 2")
                .get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(0))
                .body("pageSize", equalTo(1))
                .body("totalPages", equalTo(1))
                .body("totalSize", equalTo(1))
                .body("content", hasSize(1))
                .body("content.id", not(hasItems(nullValue())))
                .body("content.stringProperty", hasItems("Example 2"));
    }

}
