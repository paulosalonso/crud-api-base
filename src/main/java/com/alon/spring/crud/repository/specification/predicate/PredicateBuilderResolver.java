package com.alon.spring.crud.repository.specification.predicate;

import com.alon.querydecoder.MatchType;

public interface PredicateBuilderResolver {

    public static PredicateBuilder resolve(MatchType matchType) {
        
        switch(matchType) {
            case BT: return BetweenPredicateBuilder.getInstance();
            case CT: return ContainsPredicateBuilder.getInstance();
            case EQ: return EqualPredicateBuilder.getInstance();
            case GT: return GreaterThanPredicateBuilder.getInstance();
            case GTE: return GreaterThanOrEqualPredicateBuilder.getInstance();
            case IN: return InPredicateBuilder.getInstance();
            case LT: return LessThanPredicateBuilder.getInstance();
            case LTE: return LessThanOrEqualPredicateBuilder.getInstance();
            default: return EqualPredicateBuilder.getInstance();
        }
        
    }
    
}
