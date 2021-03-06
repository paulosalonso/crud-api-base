package com.alon.spring.crud.api.projection;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.alon.spring.crud.domain.service.exception.ProjectionException;

/**
 * 
 * @param <I> Input data to be projected
 * @param <O> Ouput projection
 */
public interface Projector<I, O> {
    
    public O project(I input) throws ProjectionException;
    
    default Set<String> requiredExpand() {
        return Collections.emptySet();
    }
    
}
