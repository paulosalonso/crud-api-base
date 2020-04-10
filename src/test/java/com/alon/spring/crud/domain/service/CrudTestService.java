package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.EntityTest;
import com.alon.spring.crud.domain.repository.EntityTestRepository;
import org.springframework.stereotype.Service;

@Service
public class CrudTestService implements CrudService<Long, EntityTest, EntityTestRepository> {

    private EntityTestRepository repository;

    public CrudTestService(EntityTestRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityTestRepository getRepository() {
        return repository;
    }
}
