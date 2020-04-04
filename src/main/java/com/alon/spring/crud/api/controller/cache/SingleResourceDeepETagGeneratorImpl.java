package com.alon.spring.crud.api.controller.cache;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.BaseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.util.Date;

@Component
public class SingleResourceDeepETagGeneratorImpl implements SingleResourceDeepETagGenerator {

    @Override
    public String generate(Class<? extends BaseEntity<?>> entityType,
            EntityManager entityManager, SearchInput search) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = builder.createQuery();
        Root from = query.from(entityType);
        query.multiselect(from.get("updateTimestamp"));

        Specification specification = search.toSpecification();

        if (specification != null) {
            Predicate predicate = specification.toPredicate(from, query, builder);

            if (predicate != null)
                query.where(specification.toPredicate(from, query, builder));
        }

        Object lastUpdate;

        try {
            lastUpdate = entityManager.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            lastUpdate = null;
        }
        
        if (lastUpdate == null)
            lastUpdate = "NOT MODIFIED";

        return String.valueOf(lastUpdate.hashCode());
    }

}
