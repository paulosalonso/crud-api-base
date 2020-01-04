package com.alon.spring.crud.resource.projection;

import com.alon.spring.crud.service.exception.ProjectionException;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @param <I> Input data to be projected
 * @param <O> Ouput projection
 */
public interface Projection<I, O> {
    
    public O project(I input) throws ProjectionException;
    
    default List<String> requiredExpand() {
        return Collections.emptyList();
    }
    
}
