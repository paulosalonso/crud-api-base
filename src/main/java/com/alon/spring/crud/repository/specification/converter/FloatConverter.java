/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.repository.specification.converter;

/**
 *
 * @author paulo
 */
public class FloatConverter implements DecoderConverter<Float> {
    
    private static FloatConverter INSTANCE;
    
    private FloatConverter() {}

    @Override
    public Float convert(String value) throws Throwable {
        return Float.valueOf(value);
    }
    
    public static FloatConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FloatConverter();
        
        return INSTANCE;
    }
    
}
