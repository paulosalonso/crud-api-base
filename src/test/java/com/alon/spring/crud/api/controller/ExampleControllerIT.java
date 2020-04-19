package com.alon.spring.crud.api.controller;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.AFTER_CREATE;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.BEFORE_CREATE;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

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

import com.alon.spring.crud.cleaner.DatabaseCleaner;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.ExampleService;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
public class ExampleControllerCreateIT {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ExampleService exampleService;

    @Before
    public void init() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        databaseCleaner.clearTables();
    }

    @Test
    public void whenCreateThenSuccess() {
        Example example = Example.of()
                .stringProperty("string-property")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(example)
        .when()
                .post("/example")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("stringProperty", equalTo(example.getStringProperty()));
    }

    @Test
    public void whenCreateWithBeforeCreateHookThenPersistWithHookUpdates() {
        exampleService.addBeforeCreateHook(this::concatStringPropertyCreate);

        Example example = Example.of()
                .stringProperty("property")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(example)
        .when()
                .post("/example")
        .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1))
                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));

        when()
                .get("/example/{id}", 1)
        .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(1))
                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));

        exampleService.clearHooks(BEFORE_CREATE);
    }

    @Test
    public void whenCreateWithAfterCreateHookThenPersistOriginalAndReturnWithHookUpdates() {
        exampleService.addAfterCreateHook(this::concatStringPropertyCreate);

        Example example = Example.of()
                .stringProperty("property")
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(example)
                .when()
                .post("/example");

        Example responseExample = response.body().as(Example.class);

        response.then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));

        when()
                .get("/example/{id}", responseExample.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(notNullValue())
                .body("stringProperty", equalTo(example.getStringProperty()));

        exampleService.clearHooks(AFTER_CREATE);
    }

    @Test
    public void whenGetAllThenReturn() {
        get("/example").then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(0));
    }

    private Example concatStringPropertyCreate(Example example) {
        example.setStringProperty(example
                .getStringProperty().concat("-concatenated-by-hook"));

        return example;
    }

}
