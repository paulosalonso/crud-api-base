package com.alon.spring.crud.java.api.controller;

import com.alon.spring.crud.api.controller.CrudController;
import com.alon.spring.crud.api.controller.input.EntityTestSearchInput;
import com.alon.spring.crud.domain.model.EntityTest;
import com.alon.spring.crud.domain.service.CrudTestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/entity-test")
public class EntityTestController
        extends CrudController<Long, EntityTest, EntityTest, EntityTest, EntityTestSearchInput, CrudTestService> {

    public EntityTestController(CrudTestService service) {
        super(service);
    }
}
