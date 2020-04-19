package com.alon.spring.crud.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alon.spring.crud.api.controller.input.ExampleSearchInput;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.ExampleService;

@RestController
@RequestMapping("/example")
public class ExampleController
        extends CrudController<Long, Example, Example, Example, ExampleSearchInput, ExampleService> {

    public ExampleController(ExampleService service) {
        super(service);
    }
}
