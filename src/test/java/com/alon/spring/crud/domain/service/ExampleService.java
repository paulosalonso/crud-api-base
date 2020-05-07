package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleCrudRepository;
import org.springframework.stereotype.Service;

@Service
public class ExampleService implements CrudService<Long, Example, ExampleCrudRepository> {

    private ExampleCrudRepository repository;

    public ExampleService(ExampleCrudRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExampleCrudRepository getRepository() {
        return repository;
    }
}
