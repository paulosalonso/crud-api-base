package com.alon.spring.crud.infrastructure.repository;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.model.NestedBaseEntity;
import com.alon.spring.crud.domain.repository.NestedRepository;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class NestedRepositoryImpl<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>>
implements NestedRepository<
        MASTER_ENTITY_ID_TYPE,
        MASTER_ENTITY_TYPE,
        NESTED_ENTITY_ID_TYPE,
        NESTED_ENTITY_TYPE> {

    private static final String EXPAND_HINT = "javax.persistence.loadgraph";
    private static final String ID_FIELD_NAME = "id";

    private final EntityManager entityManager;
    private final CriteriaBuilder builder;

    public NestedRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        builder = entityManager.getCriteriaBuilder();
    }

    private final Class<NESTED_ENTITY_TYPE> nestedEntityType = extractNestedEntityType();
    private final Class<NESTED_ENTITY_ID_TYPE> nestedEntityIdType = extractNestedEntityIdType();

    @Override
    public List<NESTED_ENTITY_TYPE> getAll(String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, List<String> expand) {
        CriteriaQuery<NESTED_ENTITY_TYPE> criteriaQuery = builder.createQuery(nestedEntityType);
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.select(from);
        criteriaQuery.where(
                builder.equal(from.get(masterFieldName).get(ID_FIELD_NAME), masterId));

        TypedQuery<NESTED_ENTITY_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        if (expand != null && !expand.isEmpty())
            typedQuery.setHint(EXPAND_HINT, new DynamicEntityGraph(expand));

        return typedQuery.getResultList();
    }

    @Override
    public Optional<NESTED_ENTITY_TYPE> getById(
            String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId, List<String> expand) {

        CriteriaQuery<NESTED_ENTITY_TYPE> criteriaQuery = builder.createQuery(nestedEntityType);
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.select(from);
        criteriaQuery.where(builder.and(
                builder.equal(from.get(masterFieldName).get(ID_FIELD_NAME), masterId),
                builder.equal(from.get(ID_FIELD_NAME), nestedId)));

        TypedQuery<NESTED_ENTITY_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        if (expand != null && !expand.isEmpty())
            typedQuery.setHint(EXPAND_HINT, new DynamicEntityGraph(expand));

        try {
            return Optional.of(typedQuery.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {
        CriteriaQuery<NESTED_ENTITY_ID_TYPE> criteriaQuery = builder.createQuery(extractNestedEntityIdType());
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.multiselect(from.get(ID_FIELD_NAME));
        criteriaQuery.where(builder.and(
                builder.equal(from.get(masterFieldName).get(ID_FIELD_NAME), masterId),
                builder.equal(from.get(ID_FIELD_NAME), nestedId)));

        TypedQuery<NESTED_ENTITY_ID_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        try {
            typedQuery.getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    private final <T extends NESTED_ENTITY_ID_TYPE> Class<T> extractNestedEntityIdType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();

        Type type = classType.getActualTypeArguments()[1];

        if (type instanceof  Class)
            return (Class<T>) classType.getActualTypeArguments()[1];

        return null;
    }

    private final <T extends BaseEntity<NESTED_ENTITY_ID_TYPE>> Class<T> extractNestedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();

        Type type = classType.getActualTypeArguments()[2];

        if (type instanceof Class)
            return (Class<T>) classType.getActualTypeArguments()[3];

        return null;
    }
}
