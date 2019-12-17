package com.alon.spring.crud.resource.dto;

/**
 * 
 * @param <D> Data to be converted
 * @param <O> Ouput resulting from conversion
 */
public interface OutputDtoConverter<D, O> {
    
    public O convert(D data);
    
}
