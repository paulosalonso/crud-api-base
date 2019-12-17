package com.alon.spring.crud.repository.specification.converter;

public interface DecoderConverter<T extends Comparable> {
    
    public T convert(String value);
    
}
