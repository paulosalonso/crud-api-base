package com.alon.spring.crud.repository.specification.predicate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class LessThanPredicateBuilder implements PredicateBuilder {
    
    private static PredicateBuilder INSTANCE;

    @Override
    public Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path path, String value) {
        
        Comparable convertedValue = this.convertValue(path, value);
        
        return criteriaBuilder.lessThan(path, convertedValue);
        
    }
    
    public static PredicateBuilder getInstance() {
        
        if (INSTANCE == null)
            INSTANCE = new LessThanPredicateBuilder();
        
        return INSTANCE;
        
    }
    
}
