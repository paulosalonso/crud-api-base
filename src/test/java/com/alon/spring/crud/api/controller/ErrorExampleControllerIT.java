package com.alon.spring.crud.api.controller;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.cleaner.DatabaseCleaner;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.ExampleService;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ErrorExampleControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ExampleService exampleService;

    @SpyBean
    private ProjectionService projectionService;

    @Before
    public void init() {
        RestAssured.port = port;
    }

    @After
    public void tearDown() {
        databaseCleaner.clearTables();
    }

    @Test
    public void whenGetAllThenReturnInternalServerError() {
        ExampleCreator.insertBatchOfExamples(1);

        get("/example-projection-error")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));
    }

    @Test
    public void whenGetByIdThenReturnInternalServerError() {
        Example example = Example.of()
                .stringProperty("property")
                .build();

        Integer id = given()
                .contentType(ContentType.JSON)
                .queryParam("projection", "no-operation-projection")
                .body(example)
                .post("/example-projection-error")
                .body()
                .path("id");

        get("/example-projection-error/{id}", id)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));
    }

}
