package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.cleaner.DatabaseCleaner;
import com.alon.spring.crud.domain.model.Example;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class ExampleControllerIT {

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
    public void whenCreateEntityThenSuccess() {
        Example entity = new Example();
        entity.setStringProperty("string-property");

        given()
                .contentType(ContentType.JSON)
                .body(entity)
        .when()
                .post("/entity-test")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", allOf(notNullValue(), equalTo(1)))
                .body("stringProperty", equalTo(entity.getStringProperty()));
    }

    @Test
    public void whenGetAllThenReturn() {
        get("/entity-test").then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(0));
    }

}
