package com.alon.spring.crud.api.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.alon.spring.crud.domain.model.Example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:error-projection-test.properties")
public class ExampleControllerFailProjectionIT {

    @LocalServerPort
    private int port;

    @Before
    public void init() {
        RestAssured.port = port;
    }

    @Test
    public void whenCreateWithNonExistentProjectionThenReturnBadRequest() {
        Example example = Example.of()
                .stringProperty("string-property")
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("projection", "nonExistentProjection")
                .body(example)
                .when()
                .post("/example")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Invalid parameter"))
                .body("detail", equalTo("Projection 'nonExistentProjection' not found"));
    }
}
