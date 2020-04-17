package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleRepository;
import org.springframework.stereotype.Service;

@Service
public class CrudTestService implements CrudService<Long, Example, ExampleRepository> {

    private ExampleRepository repository;

    public CrudTestService(ExampleRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExampleRepository getRepository() {
        return repository;
    }
}
