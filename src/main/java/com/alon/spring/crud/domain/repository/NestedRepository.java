package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.model.NestedBaseEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface NestedRepository<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>> {

    List<NESTED_ENTITY_TYPE> getAll(String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, List<String> expand);

    Optional<NESTED_ENTITY_TYPE> getById(String masterFieldName,
            MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId, List<String> expand);

    boolean existsById(String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId);
}
