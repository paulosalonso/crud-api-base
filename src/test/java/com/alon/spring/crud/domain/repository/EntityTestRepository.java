package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.EntityTest;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

public interface EntityTestRepository
    extends EntityGraphJpaRepository<EntityTest, Long>, EntityGraphJpaSpecificationExecutor<EntityTest> {}
