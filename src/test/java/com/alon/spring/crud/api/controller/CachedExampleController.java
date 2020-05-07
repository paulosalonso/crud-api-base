package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.ExampleSearchInput;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.ExampleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cached-example")
public class CachedExampleController
        extends CachedCrudController<Long, Example, Example, Example, ExampleSearchInput, ExampleService> {

    protected CachedExampleController(ExampleService service) {
        super(service);
    }
}
