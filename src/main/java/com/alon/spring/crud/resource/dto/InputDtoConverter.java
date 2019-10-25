package com.alon.spring.crud.resource.dto;

/**
 * 
 * @param <I> Input to be converted
 * @param <R> Result of conversion
 */
public interface InputDtoConverter<I, R> {
    
    public R convert(I input);
    
}
