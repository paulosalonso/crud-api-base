/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.repository.specification.converter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author paulo
 */
public abstract class ConverterResolver {
    
    private static final Map<Class<?>, DecoderConverter<?>> CONVERTERS = new HashMap<>();
    
    static {
        CONVERTERS.put(String.class, DefaultConverter.getInstance());
        CONVERTERS.put(Integer.class, IntegerConverter.getInstance());
        CONVERTERS.put(Long.class, LongConverter.getInstance());
        CONVERTERS.put(Float.class, FloatConverter.getInstance());
        CONVERTERS.put(Double.class, DoubleConverter.getInstance());
        CONVERTERS.put(BigDecimal.class, BigDecimalConverter.getInstance());
        CONVERTERS.put(Boolean.class, BooleanConverter.getInstance());
        CONVERTERS.put(Date.class, DateTimeConverter.getInstance());
    }
    
    public static <O> DecoderConverter<O> resolve(Class<O> clazz) {
        DecoderConverter converter = CONVERTERS.get(clazz);
        
        if (converter == null)
            converter = DefaultConverter.getInstance();
        
        return converter;
    }
    
}
