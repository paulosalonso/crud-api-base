package com.alon.spring.crud.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alon.spring.crud.api.controller.input.ExampleSearchInput;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.ExampleService;

@RestController
@RequestMapping("/example-projection-error")
public class ErrorExampleController
        extends CrudController<Long, Example, Example, Example, ExampleSearchInput, ExampleService> {

    public ErrorExampleController(ExampleService service) {
        super(service);
    }

    @Override
    protected String getSingleDefaultProjection() {
        return "errorExampleProjection";
    }

    @Override
    protected String getCollectionDefaultProjection() {
        return "errorExampleProjection";
    }
}
