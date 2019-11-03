package com.alon.spring.crud.repository.specification.predicate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class ContainsPredicateBuilder implements PredicateBuilder {
    
    private static PredicateBuilder INSTANCE;

    @Override
    public Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path path, String value) {
        
        value = String.format("%%%s%%", this.convertValue(path, value));
        
        return criteriaBuilder.like(path, value);
        
    }
    
    public static PredicateBuilder getInstance() {
        
        if (INSTANCE == null)
            INSTANCE = new ContainsPredicateBuilder();
        
        return INSTANCE;
        
    }
    
}
