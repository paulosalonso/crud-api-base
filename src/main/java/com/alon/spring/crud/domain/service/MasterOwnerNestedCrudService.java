package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.repository.CrudRepository;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface MasterOwnerNestedCrudService<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        MASTER_REPOSITORY_TYPE extends CrudRepository<MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>>
extends NestedCrudService<
        MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE,
        NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE> {

    String ID_FIELD_NAME = "id";
    ModelMapper MAPPER = new ModelMapper();


    Supplier<Collection<NESTED_ENTITY_TYPE>> getNestedGetter(MASTER_ENTITY_TYPE masterEntity);
    Consumer<NESTED_ENTITY_TYPE> getNestedSetter(MASTER_ENTITY_TYPE masterEntity);
    MASTER_REPOSITORY_TYPE getMasterRepository();
    String getNestedFieldName();

    @Override
    default Collection<NESTED_ENTITY_TYPE> getAll(MASTER_ENTITY_ID_TYPE masterId, List<String> expand) {
        Optional<MASTER_ENTITY_TYPE> masterEntityOpt;

        if (expand != null && !expand.isEmpty())
            masterEntityOpt = getMasterRepository().findById(masterId, new DynamicEntityGraph(expand));
        else
            masterEntityOpt = getMasterRepository().findById(masterId);

        return masterEntityOpt
                .map(this::getNestedGetter)
                .map(Supplier::get)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Master resource with id %s was not found", masterId)));
    }

    @Override
    default NESTED_ENTITY_TYPE read(
            MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId, List<String> expand) {

        Specification<MASTER_ENTITY_TYPE> specification = getFindByIdSpecification(masterId, nestedId);

        Optional<MASTER_ENTITY_TYPE> masterEntityOpt;

        if (expand != null && !expand.isEmpty())
            masterEntityOpt = getMasterRepository().findOne(specification, new DynamicEntityGraph(expand));
        else
            masterEntityOpt = getMasterRepository().findOne(specification);

        return masterEntityOpt
                .map(this::getNestedGetter)
                .map(Supplier::get)
                .map(Collection::stream)
                .map(Stream::findFirst)
                .map(Optional::get)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s", masterId, nestedId)));
    }

    @Override
    default NESTED_ENTITY_TYPE create(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_TYPE nestedEntity) {
        MASTER_ENTITY_TYPE masterEntity = getMasterRepository()
                .findById(masterId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Master resource with id %s was not found", masterId)));

        getNestedSetter(masterEntity).accept(nestedEntity);

        masterEntity = getMasterRepository().save(masterEntity);

        Collection<NESTED_ENTITY_TYPE> nestedCollection = getNestedGetter(masterEntity).get();

        return nestedCollection.stream()
                .skip(nestedCollection.size() - 1)
                .findFirst()
                .get();
    }

    @Override
    default NESTED_ENTITY_TYPE update(MASTER_ENTITY_ID_TYPE masterId,
            NESTED_ENTITY_ID_TYPE nestedId, NESTED_ENTITY_TYPE nestedEntity) {

        Specification<MASTER_ENTITY_TYPE> specification = getFindByIdSpecification(masterId, nestedId);

        Optional<MASTER_ENTITY_TYPE> masterEntityOpt = getMasterRepository().findOne(specification);

        NESTED_ENTITY_TYPE persistedNestedEntity = masterEntityOpt
                .map(this::getNestedGetter)
                .map(Supplier::get)
                .map(Collection::stream)
                .map(Stream::findFirst)
                .map(Optional::get)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s", masterId, nestedId)));

        MAPPER.map(nestedEntity, persistedNestedEntity);

        getMasterRepository().save(masterEntityOpt.get());

        return persistedNestedEntity;
    }

    @Override
    default void delete(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {

        MASTER_ENTITY_TYPE masterEntity = getMasterRepository()
                .findOne(getFindByIdSpecification(masterId, nestedId))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Resource not found with masterId %s and nestedId %s",
                                masterId, nestedId)));

        getNestedGetter(masterEntity).get().clear();

        getMasterRepository().save(masterEntity);
    }

    default Specification<MASTER_ENTITY_TYPE> getFindByIdSpecification(MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {
        return ((root, query, criteriaBuilder) -> {
            Join<MASTER_ENTITY_TYPE, NESTED_ENTITY_TYPE> nested = root.join(getNestedFieldName());
            return criteriaBuilder.and(criteriaBuilder.equal(root.get(ID_FIELD_NAME), masterId),
                    criteriaBuilder.equal(nested.get(ID_FIELD_NAME), nestedId));
        });
    }
}
