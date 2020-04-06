package com.alon.spring.crud.api.controller.input;

/**
 * 
 * @param <I> Input type
 * @param <O> Output type
 */
public interface InputMapper<I, O> {
    
    public O map(I input);
    
}
