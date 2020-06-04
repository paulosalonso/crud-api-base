package com.alon.spring.crud.domain.repository;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.support.EntityGraphSimpleJpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.unsorted;

public class NestedRepository<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>>
extends EntityGraphSimpleJpaRepository<NESTED_ENTITY_TYPE, NESTED_ENTITY_ID_TYPE> {

    private static final String EXPAND_HINT = "javax.persistence.loadgraph";

    protected final EntityManager entityManager;
    protected final CriteriaBuilder builder;
    private final String masterIdFieldName;
    private final String nestedIdFieldName;

    /**
     * Use "id" as the field name for the master and nested id fields
     */
    public NestedRepository(Class<NESTED_ENTITY_TYPE> entityType, EntityManager entityManager) {
        this(entityType, entityManager, "id", "id");
    }

    public NestedRepository(Class<NESTED_ENTITY_TYPE> entityType,
            EntityManager entityManager, String masterIdFieldName, String nestedIdFieldName) {

        super(entityType, entityManager);
        this.entityManager = entityManager;
        builder = entityManager.getCriteriaBuilder();
        this.masterIdFieldName = masterIdFieldName;
        this.nestedIdFieldName = nestedIdFieldName;
    }

    private final Class<NESTED_ENTITY_TYPE> nestedEntityType = extractNestedEntityType();
    private final Class<NESTED_ENTITY_ID_TYPE> nestedEntityIdType = extractNestedEntityIdType();

    public List<NESTED_ENTITY_TYPE> search(
            String masterFieldName, MASTER_ENTITY_ID_TYPE masterId, SearchCriteria searchCriteria) {

        CriteriaQuery<NESTED_ENTITY_TYPE> criteriaQuery = builder.createQuery(nestedEntityType);
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.select(from);

        Predicate predicate = builder.equal(from.get(masterFieldName).get(masterIdFieldName), masterId);

        if (searchCriteria.getFilter() != null)
            predicate = builder.and(predicate,
                    searchCriteria.getFilter().toPredicate(from, criteriaQuery, builder));

        criteriaQuery.where(predicate);

        if (searchCriteria.getPageable() != null) {
            Sort sort = searchCriteria
                    .getPageable()
                    .getSortOr(unsorted());

            List<Order> orders = QueryUtils.toOrders(sort, from, builder);

            criteriaQuery.orderBy(orders);
        }

        TypedQuery<NESTED_ENTITY_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        if (searchCriteria.getPageable() != null) {
            typedQuery.setFirstResult((int) searchCriteria.getPageable().getOffset());
            typedQuery.setMaxResults(searchCriteria.getPageable().getPageSize());
        }

        if (searchCriteria.getExpand() != null)
            typedQuery.setHint(EXPAND_HINT, searchCriteria.getExpand());

        return typedQuery.getResultList();
    }

    public Optional<NESTED_ENTITY_TYPE> getById(String masterFieldName,
            MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId, List<String> expand) {

        CriteriaQuery<NESTED_ENTITY_TYPE> criteriaQuery = builder.createQuery(nestedEntityType);
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.select(from);
        criteriaQuery.where(builder.and(
                builder.equal(from.get(masterFieldName).get(masterIdFieldName), masterId),
                builder.equal(from.get(nestedIdFieldName), nestedId)));

        TypedQuery<NESTED_ENTITY_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        if (expand != null && !expand.isEmpty())
            typedQuery.setHint(EXPAND_HINT, new DynamicEntityGraph(expand));

        try {
            return Optional.of(typedQuery.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    public boolean existsById(String masterFieldName,
            MASTER_ENTITY_ID_TYPE masterId, NESTED_ENTITY_ID_TYPE nestedId) {

        CriteriaQuery<NESTED_ENTITY_ID_TYPE> criteriaQuery = builder.createQuery(extractNestedEntityIdType());
        Root<NESTED_ENTITY_TYPE> from = criteriaQuery.from(nestedEntityType);
        criteriaQuery.multiselect(from.get(nestedIdFieldName));
        criteriaQuery.where(builder.and(
                builder.equal(from.get(masterFieldName).get(masterIdFieldName), masterId),
                builder.equal(from.get(nestedIdFieldName), nestedId)));

        TypedQuery<NESTED_ENTITY_ID_TYPE> typedQuery = entityManager.createQuery(criteriaQuery);

        try {
            typedQuery.getSingleResult();
            return true;
        } catch (NoResultException ex) {
            return false;
        }
    }

    private String getIdFieldName() {
        return "id";
    }

    private final <T extends NESTED_ENTITY_ID_TYPE> Class<T> extractNestedEntityIdType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();

        Type type = classType.getActualTypeArguments()[1];

        if (type instanceof  Class)
            return (Class<T>) type;

        return null;
    }

    private final <T extends BaseEntity<NESTED_ENTITY_ID_TYPE>> Class<T> extractNestedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();

        Type type = classType.getActualTypeArguments()[2];

        if (type instanceof Class)
            return (Class<T>) type;

        return null;
    }
}
