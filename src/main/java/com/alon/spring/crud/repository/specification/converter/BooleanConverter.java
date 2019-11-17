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
public class BooleanConverter implements DecoderConverter<Boolean> {
    
    private static BooleanConverter INSTANCE;
    
    private BooleanConverter() {}

    @Override
    public Boolean convert(String value) {
        return Boolean.valueOf(value);
    }
    
    public static BooleanConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new BooleanConverter();
        
        return INSTANCE;
    }
    
}
