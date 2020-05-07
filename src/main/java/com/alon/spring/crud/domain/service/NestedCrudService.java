package com.alon.spring.crud.domain.service;

import java.util.Collection;
import java.util.List;

public interface NestedCrudService<
        MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE,
        NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE> {

    Collection<NESTED_ENTITY_TYPE> getAll(MASTER_ENTITY_ID_TYPE masterId, List<String> expand);

    NESTED_ENTITY_TYPE create(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_TYPE nestedEntity);

    NESTED_ENTITY_TYPE read(MASTER_ENTITY_ID_TYPE masterId,
            NESTED_ENTITY_ID_TYPE nestedId, List<String> expand);

    NESTED_ENTITY_TYPE update(MASTER_ENTITY_ID_TYPE masterId,
          NESTED_ENTITY_ID_TYPE nestedId, NESTED_ENTITY_TYPE nestedEntity);

    void delete(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId);
}
