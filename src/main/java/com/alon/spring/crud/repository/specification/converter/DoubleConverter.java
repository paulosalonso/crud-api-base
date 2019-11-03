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
public class DoubleConverter implements DecoderConverter<Double> {
    
    private static DoubleConverter INSTANCE;
    
    private DoubleConverter() {}

    @Override
    public Double convert(String value) {
        return Double.valueOf(value);
    }
    
    public static DoubleConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DoubleConverter();
        
        return INSTANCE;
    }
    
}
