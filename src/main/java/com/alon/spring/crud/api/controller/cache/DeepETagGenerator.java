package com.alon.spring.crud.api.controller.cache;

import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.domain.model.BaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

public interface DeepETagGenerator {
	String generate(Class<? extends BaseEntity<?>> entityType, EntityManager entityManager, SearchInput search);
}