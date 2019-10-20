package com.alon.spring.crud.resource.dto;

/**
 * 
 * @param <I> Input to be converted
 * @param <R> Result of conversion
 */
public interface InputDtoConverter<I extends InputDto, R> {
    
    public R convert(I input);
    
}
