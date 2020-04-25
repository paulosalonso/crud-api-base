package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.domain.model.Example;
import io.restassured.http.ContentType;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;

public final class ExampleCreator {

    private ExampleCreator() {}

    protected static void insertBatchOfExamples(int quantity) {
        for (int i = 1; i <= quantity; i++) {
            Example example = Example.of()
                    .stringProperty("Example " + i)
                    .build();

            given()
                    .contentType(ContentType.JSON)
                    .body(example)
                    .post("/example")
                    .then()
                    .statusCode(HttpStatus.CREATED.value());
        }
    }

}
