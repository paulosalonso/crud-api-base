package com.alon.spring.crud.resource.dto;

/**
 * 
 * @param <I> Input data to be projected
 * @param <O> Ouput projection
 */
public interface Projection<I, O> {
    
    public O project(I input);
    
}
