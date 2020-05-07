package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

import java.io.Serializable;

public interface CrudRepository<ENTITY_ID_TYPE extends Serializable, ENTITY_TYPE extends BaseEntity<ENTITY_ID_TYPE>>
        extends EntityGraphJpaRepository<ENTITY_TYPE, ENTITY_ID_TYPE>, EntityGraphJpaSpecificationExecutor<ENTITY_TYPE> {
}
