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

public interface NestedAsOwnerNestedCrudService<
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

    ModelMapper UPDATE_MAPPER = new ModelMapper();

    MASTER_REPOSITORY_TYPE getMasterRepository();
    NESTED_REPOSITORY_TYPE getNestedRepository();

    @Override
    default Collection<NESTED_ENTITY_TYPE> search(MASTER_ENTITY_ID_TYPE masterId, SearchCriteria searchCriteria) {
        return getNestedRepository().search(masterId, searchCriteria);
    }

    @Override
    default NESTED_ENTITY_TYPE read(MASTER_ENTITY_ID_TYPE masterId,
                                    NESTED_ENTITY_ID_TYPE nestedId, List<String> expand) {

        executeBeforeReadHooks(nestedId, masterId);

        NESTED_ENTITY_TYPE entity =  getNestedRepository()
                .findById(masterId, nestedId, expand)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s", masterId, nestedId)));

        executeAfterReadHooks(entity, masterId);

        return entity;
    }

    @Override
    default NESTED_ENTITY_TYPE create(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_TYPE nestedEntity) {
        executeBeforeCreateHooks(nestedEntity, masterId);

        MASTER_ENTITY_TYPE masterEntity = getMasterRepository()
                .findById(masterId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Master resource with id %s was not found", masterId)));

        nestedEntity.setMasterEntity(masterEntity);

        nestedEntity = getNestedRepository().save(nestedEntity);

        executeAfterCreateHooks(nestedEntity, masterId);

        return nestedEntity;
    }

    @Override
    default NESTED_ENTITY_TYPE update(MASTER_ENTITY_ID_TYPE masterId,
            NESTED_ENTITY_ID_TYPE nestedId, NESTED_ENTITY_TYPE nestedEntity) {

        executeBeforeUpdateHooks(nestedEntity, masterId);

        if (nestedEntity.getMasterEntity() != null && nestedEntity.getMasterEntity().getId().equals(nestedId))
            throw new UpdateException("Master entity must not be changed in the update.");

        NESTED_ENTITY_TYPE persistedNestedEntity = getNestedRepository()
                .findById(masterId, nestedId, null)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s",
                                masterId, nestedId)));

        UPDATE_MAPPER.map(nestedEntity, persistedNestedEntity);

        persistedNestedEntity = getNestedRepository().save(persistedNestedEntity);

        executeAfterUpdateHooks(persistedNestedEntity, masterId);

        return persistedNestedEntity;
    }

    @Override
    default void delete(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {
        executeBeforeDeleteHooks(nestedId, masterId);

        boolean exists = getNestedRepository()
                .existsById(masterId, nestedId);

        if (!exists)
            new NotFoundException(
                    String.format("Resource not found with masterId %s and nestedId %s",
                            masterId, nestedId));

        getNestedRepository().deleteById(nestedId);

        executeAfterDeleteHooks(nestedId, masterId);
    }

    default ModelMapper getUpdateMapper() {
        if (UPDATE_MAPPER.getConfiguration().getPropertyCondition() == null) {
            UPDATE_MAPPER.getConfiguration().setPropertyCondition(context ->
                    !context.getMapping()
                            .getLastDestinationProperty()
                            .getName()
                            .equals(getNestedRepository().getMasterFieldName()));
        }

        return UPDATE_MAPPER;
    }
}
