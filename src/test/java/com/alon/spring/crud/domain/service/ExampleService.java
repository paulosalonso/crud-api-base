package com.alon.spring.crud.domain.service;

import javax.validation.Valid;

import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.repository.ExampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExampleService implements CrudService<Long, Example, ExampleRepository> {

    private ExampleRepository repository;

    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExampleRepository getRepository() {
        return repository;
    }
}
