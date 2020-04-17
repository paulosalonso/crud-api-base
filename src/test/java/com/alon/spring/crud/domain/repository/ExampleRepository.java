package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.Example;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

public interface ExampleRepository
    extends EntityGraphJpaRepository<Example, Long>, EntityGraphJpaSpecificationExecutor<Example> {}
