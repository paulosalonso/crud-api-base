package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.CrudController;
import com.alon.spring.crud.api.controller.input.ExampleSearchInput;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.CrudTestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/example")
public class ExampleController
        extends CrudController<Long, Example, Example, Example, ExampleSearchInput, CrudTestService> {

    public ExampleController(CrudTestService service) {
        super(service);
    }
}
