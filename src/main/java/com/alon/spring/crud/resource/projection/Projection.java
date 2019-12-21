package com.alon.spring.crud.resource.projection;

import com.alon.spring.crud.service.exception.ProjectionException;

/**
 * 
 * @param <I> Input data to be projected
 * @param <O> Ouput projection
 */
public interface Projection<I, O> {
    
    public O project(I input) throws ProjectionException;
    
}
