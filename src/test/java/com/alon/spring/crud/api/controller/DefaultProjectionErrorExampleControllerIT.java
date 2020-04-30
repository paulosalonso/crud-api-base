package com.alon.spring.crud.api.controller;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItems;

import io.restassured.response.Response;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultProjectionErrorExampleControllerIT {

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
        Example example = Example.of()
                .stringProperty("property")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(example)
                .when()
                .post("/example-projection-error")
                .then()
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));

        when()
                .get("/example-projection-error")
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

        when()
                .get("/example-projection-error/{id}", id)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));
    }

    @Test
    public void whenCreateThenPersistButReturnInternalServerError() {
        Example example = Example.of()
                .stringProperty("Example with projection error")
                .build();

        Response response = given()
                .contentType(ContentType.JSON)
                .body(example)
                .post("/example-projection-error");

        response.then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));

        given()
                .queryParam("stringProperty", "Example with projection error")
                .get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1))
                .body("content[0].id", notNullValue())
                .body("content[0].stringProperty", equalTo("Example with projection error"));
    }

    @Test
    public void whenUpdateThenPersistButReturnInternalServerError() {
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

        example.setStringProperty("updated-property");

        given()
                .contentType(ContentType.JSON)
                .body(example)
                .when()
                .put("/example-projection-error/{id}", id)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("status", equalTo(500))
                .body("title", equalTo("Internal error"))
                .body("detail", equalTo("An error occurred when projecting the response. If the problem persists, contact your administrator."));

        when()
                .get("/example/{id}", id)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(id))
                .body("stringProperty", equalTo(example.getStringProperty()));
    }

}
