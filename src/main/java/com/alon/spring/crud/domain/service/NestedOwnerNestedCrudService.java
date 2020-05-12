package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.model.NestedBaseEntity;
import com.alon.spring.crud.domain.repository.CrudRepository;
import com.alon.spring.crud.domain.repository.NestedRepository;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import org.modelmapper.ModelMapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface NestedOwnerNestedCrudService<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        MASTER_REPOSITORY_TYPE extends CrudRepository<MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends NestedBaseEntity<NESTED_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE>,
        NESTED_REPOSITORY_TYPE extends
                NestedRepository<MASTER_ENTITY_ID_TYPE, NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE>>
    extends NestedCrudService<
        MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE,
        NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE> {

    ModelMapper MAPPER = new ModelMapper();

    MASTER_REPOSITORY_TYPE getMasterRepository();
    NESTED_REPOSITORY_TYPE getNestedRepository();
    String getMasterFieldName();

    @Override
    default Collection<NESTED_ENTITY_TYPE> getAll(MASTER_ENTITY_ID_TYPE masterId, List<String> expand) {
        return getNestedRepository().getAll(getMasterFieldName(), masterId , expand);
    }

    @Override
    default NESTED_ENTITY_TYPE read(
            MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId, List<String> expand) {

        return getNestedRepository()
                .getById(getMasterFieldName(), masterId, nestedId, expand)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s", masterId, nestedId)));
    }

    @Override
    default NESTED_ENTITY_TYPE create(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_TYPE nestedEntity) {

        MASTER_ENTITY_TYPE masterEntity = getMasterRepository()
                .findById(masterId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Master resource with id %s was not found", masterId)));

        nestedEntity.setMasterEntity(masterEntity);

        return getNestedRepository().save(nestedEntity);
    }

    @Override
    default NESTED_ENTITY_TYPE update(MASTER_ENTITY_ID_TYPE masterId,
            NESTED_ENTITY_ID_TYPE nestedId, NESTED_ENTITY_TYPE nestedEntity) {

        if (nestedEntity.getMasterEntity() != null && nestedEntity.getMasterEntity().getId().equals(nestedId))
            throw new UpdateException("Master entity must not be changed in the update.");

        NESTED_ENTITY_TYPE persistedNestedEntity = getNestedRepository()
                .getById(getMasterFieldName(), masterId, nestedId, null)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s",
                                masterId, nestedId)));

        MAPPER.map(nestedEntity, persistedNestedEntity);

        return getNestedRepository().save(persistedNestedEntity);
    }

    @Override
    default void delete(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {
        boolean exists = getNestedRepository()
                .existsById(getMasterFieldName(), masterId, nestedId);

        if (!exists)
            new NotFoundException(
                    String.format("Resource not found with masterId %s and nestedId %s",
                            masterId, nestedId));

        getNestedRepository().deleteById(nestedId);
    }
}
