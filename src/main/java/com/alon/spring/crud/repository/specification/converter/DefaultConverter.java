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
public class DefaultConverter implements DecoderConverter<String> {
    
    private static DefaultConverter INSTANCE;

    @Override
    public String convert(String value) throws Throwable {
        return value;
    }
    
    public static DefaultConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DefaultConverter();
        
        return INSTANCE;
    }
    
}
