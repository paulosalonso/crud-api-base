package com.alon.spring.crud.api.controller.input.mapper;

/**
 * 
 * @param <I> Input type
 * @param <O> Output type
 */
public interface InputMapper<I, O> {
    
    O map(I input);
    void map(I input, O output);
    
}
