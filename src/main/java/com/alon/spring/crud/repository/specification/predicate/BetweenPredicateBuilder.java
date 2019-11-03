package com.alon.spring.crud.repository.specification.predicate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class BetweenPredicateBuilder implements PredicateBuilder {
    
    private static PredicateBuilder INSTANCE;

    @Override
    public Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path path, String value) {
        Object[] values = this.splitValues(path, value);
        
        return criteriaBuilder.between(path, (Comparable) values[0], (Comparable) values[1]);
    }
    
    private Object[] splitValues(Path path, String value) {
        Object[] values = value.split("-");
        
        if (values.length != 2)
            throw new IllegalArgumentException("For the between comparation a hyphen separated string with two values is needed.");
        
        values[0] = this.convertValue(path, values[0].toString());
        values[1] = this.convertValue(path, values[1].toString());
        
        return values;
    }
    
    public static PredicateBuilder getInstance() {
        
        if (INSTANCE == null)
            INSTANCE = new BetweenPredicateBuilder();
        
        return INSTANCE;
        
    }
    
}
