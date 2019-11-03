package com.alon.spring.crud.repository.specification.predicate;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

public class InPredicateBuilder implements PredicateBuilder {
    
    private static PredicateBuilder INSTANCE;

    @Override
    public Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path path, String value) {
        
        List<Comparable> values = Stream.of(value.split(","))
                                        .map(this::convertValue)
                                        .collect(Collectors.toList());
                
        return path.in(values);
        
    }
    
    public static PredicateBuilder getInstance() {
        
        if (INSTANCE == null)
            INSTANCE = new InPredicateBuilder();
        
        return INSTANCE;
        
    }
    
}
