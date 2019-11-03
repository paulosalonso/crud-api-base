package com.alon.spring.crud.repository.specification.converter;

public class DefaultConverter implements DecoderConverter<String> {
    
    private static DefaultConverter INSTANCE;

    @Override
    public String convert(String value) {
        return value;
    }
    
    public static DefaultConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DefaultConverter();
        
        return INSTANCE;
    }
    
}
