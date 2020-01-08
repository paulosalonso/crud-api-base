package com.alon.spring.crud.resource.input;

/**
 * 
 * @param <I> Input to be converted
 * @param <R> Result of conversion
 */
public interface InputMapper<I, R> {
    
    public R map(I input);
    
}