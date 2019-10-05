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
public class IntegerConverter implements DecoderConverter<Integer> {
    
    private static IntegerConverter INSTANCE;
    
    private IntegerConverter() {}

    @Override
    public Integer convert(String value) throws Throwable {
        return Integer.valueOf(value);
    }
    
    public static IntegerConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new IntegerConverter();
        
        return INSTANCE;
    }
    
}
