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
public class LongConverter implements DecoderConverter<Long> {
    
    private static LongConverter INSTANCE;
    
    private LongConverter() {}

    @Override
    public Long convert(String value) {
        return Long.valueOf(value);
    }
    
    public static LongConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new LongConverter();
        
        return INSTANCE;
    }
    
}
