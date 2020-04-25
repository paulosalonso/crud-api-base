package com.alon.spring.crud.api.controller;

import static com.alon.spring.crud.api.controller.ExampleCreator.insertBatchOfExamples;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.AFTER_CREATE;
import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.BEFORE_CREATE;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

import com.alon.spring.crud.api.controller.output.OutputPage;
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
public class ExampleControllerIT {

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
    public void whenGetAllThenReturn() {
        insertBatchOfExamples(3);

        get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(0))
                .body("pageSize", equalTo(3))
                .body("totalPages", equalTo(1))
                .body("totalSize", equalTo(3))
                .body("content", hasSize(3))
                .body("content.id", not(hasItems(nullValue())))
                .body("content.stringProperty", hasItems("Example 1", "Example 2", "Example 3"));
    }

    @Test
    public void whenGetAllPaginatedThenReturn() {
        insertBatchOfExamples(3);

        given()
                .queryParam("page", 0)
                .queryParam("size", 1)
                .get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(0))
                .body("pageSize", equalTo(1))
                .body("totalPages", equalTo(3))
                .body("totalSize", equalTo(3))
                .body("content", hasSize(1))
                .body("content.id", not(hasItems(nullValue())))
                .body("content.stringProperty", hasItems("Example 1"));

        given()
                .queryParam("page", 1)
                .queryParam("size", 1)
                .get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(1))
                .body("pageSize", equalTo(1))
                .body("totalPages", equalTo(3))
                .body("totalSize", equalTo(3))
                .body("content", hasSize(1))
                .body("content.id", not(hasItems(nullValue())))
                .body("content.stringProperty", hasItems("Example 2"));

        given()
                .queryParam("page", 2)
                .queryParam("size", 1)
                .get("/example")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page", equalTo(2))
                .body("pageSize", equalTo(1))
                .body("totalPages", equalTo(3))
                .body("totalSize", equalTo(3))
                .body("content", hasSize(1))
                .body("content.id", not(hasItems(nullValue())))
                .body("content.stringProperty", hasItems("Example 3"));
    }

    @Test
    public void whenGetFilteredByPropertyThenReturn() {
        insertBatchOfExamples(3);

        given()
                .queryParam("stringProperty", "Example 2")
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

    @Test
    public void whenGetFilteredByExpressionThenReturnLocked() {
        given()
                .queryParam("filter", "stringProperty:Example 1")
                .get("/example")
                .then()
                .statusCode(HttpStatus.LOCKED.value())
                .body("status", equalTo(423))
                .body("title", equalTo("Locked resource"))
                .body("detail", equalTo("The filter by expression feature is not enabled."));
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
    public void whenCreateWithProjectionThenSuccess() {
        Example example = Example.of()
                .stringProperty("string-property")
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("projection", "exampleProjection")
                .body(example)
                .when()
                .post("/example")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("property", equalTo(example.getStringProperty()));
    }

    @Test
    public void whenCreateWithNonExistentProjectionThenUseDefaultProjection() {
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
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("stringProperty", equalTo(example.getStringProperty()));
    }

//    @Test
//    public void whenCreateWithBeforeCreateHookThenPersistWithHookUpdates() {
//        exampleService.addBeforeCreateHook(this::concatStringPropertyCreate);
//
//        Example example = Example.of()
//                .stringProperty("property")
//                .build();
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(example)
//        .when()
//                .post("/example")
//        .then()
//                .statusCode(HttpStatus.CREATED.value())
//                .body("id", equalTo(1))
//                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));
//
//        when()
//                .get("/example/{id}", 1)
//        .then()
//                .statusCode(HttpStatus.OK.value())
//                .body("id", equalTo(1))
//                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));
//
//        exampleService.clearHooks(BEFORE_CREATE);
//    }

//    @Test
//    public void whenCreateWithAfterCreateHookThenPersistOriginalAndReturnWithHookUpdates() {
//        exampleService.addAfterCreateHook(this::concatStringPropertyCreate);
//
//        Example example = Example.of()
//                .stringProperty("property")
//                .build();
//
//        Response response = given()
//                .contentType(ContentType.JSON)
//                .body(example)
//                .when()
//                .post("/example");
//
//        Example responseExample = response.body().as(Example.class);
//
//        response.then()
//                .statusCode(HttpStatus.CREATED.value())
//                .body("id", notNullValue())
//                .body("stringProperty", equalTo(example.getStringProperty().concat("-concatenated-by-hook")));
//
//        when()
//                .get("/example/{id}", responseExample.getId())
//                .then()
//                .statusCode(HttpStatus.OK.value())
//                .body(notNullValue())
//                .body("stringProperty", equalTo(example.getStringProperty()));
//
//        exampleService.clearHooks(AFTER_CREATE);
//    }

    @Test
    public void whenCreateWithInvalidRequestBodyThenReturnBadRequest() {
        given()
                .contentType("application/json")
                .body("{\"stringProperty:\"property value\"")
                .post("/example")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Unrecognized message"))
                .body("detail", equalTo("Invalid request body."));
    }

    @Test
    public void whenCreateWithInvalidPropertyNameThenReturnBadRequest() {
        given()
                .contentType("application/json")
                .body("{\"property\":\"property value\"")
                .post("/example")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Unrecognized message"))
                .body("detail", equalTo("Invalid request body."));
    }

    @Test
    public void whenCreateWithoutRequiredPropertyThenReturnBadRequest() {
        given()
                .contentType("application/json")
                .body(Example.of().build())
                .post("/example")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("status", equalTo(400))
                .body("title", equalTo("Invalid data"))
                .body("detail", equalTo("Invalid fields."))
                .body("violations", hasSize(1))
                .body("violations[0].context", equalTo("stringProperty"))
                .body("violations[0].message", equalTo("não pode estar em branco"));
    }

//    private Example concatStringPropertyCreate(Example example) {
//        example.setStringProperty(example
//                .getStringProperty().concat("-concatenated-by-hook"));
//
//        return example;
//    }

//    private void insertBatchOfExamples(int quantity) {
//        for (int i = 1; i <= quantity; i++) {
//            Example example = Example.of()
//                    .stringProperty("Example " + i)
//                    .build();
//
//            given()
//                    .contentType(ContentType.JSON)
//                    .body(example)
//                    .post("/example")
//                    .then()
//                    .statusCode(HttpStatus.CREATED.value());
//        }
//    }

}
