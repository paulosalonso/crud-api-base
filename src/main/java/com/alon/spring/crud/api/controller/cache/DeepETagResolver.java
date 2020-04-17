package com.alon.spring.crud.api.controller.cache;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
public class DeepETagResolver {

    private EntityManager entityManager;
    private DeepETagGenerator singleResourceDeepETagGenerator;
    private DeepETagGenerator collectionResourceETagGenerator;

    @Autowired
    public DeepETagResolver(EntityManager entityManager,
                            SingleResourceDeepETagGenerator singleResourceDeepETagGenerator,
                            CollectionResourceDeepETagGenerator collectionResourceETagGenerator) {

        this.entityManager = entityManager;
        this.singleResourceDeepETagGenerator = singleResourceDeepETagGenerator;
        this.collectionResourceETagGenerator = collectionResourceETagGenerator;
    }

    public <ID> String generateSingleResourceETag(Class<? extends BaseEntity<?>> entityType, ID id) {
        SearchInput search = new SearchInput() {
            @Override
            public Specification toSpecification() {
                return (root, query, builder) -> builder.equal(root.get("id"), id);
            }
        };

        return singleResourceDeepETagGenerator.generate(entityType, entityManager, search);
    }

    public String generateCollectionResourceETag(Class<? extends BaseEntity<?>> entityType, SearchInput search) {
        return collectionResourceETagGenerator.generate(entityType, entityManager, search);
    }
}
