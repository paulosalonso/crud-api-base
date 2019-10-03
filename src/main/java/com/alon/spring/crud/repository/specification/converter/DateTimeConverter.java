/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.repository.specification.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author paulo
 */
public class DateTimeConverter implements DecoderConverter<Date> {
    
    private static DateTimeConverter INSTANCE;
    
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private DateTimeConverter() {}

    @Override
    public Date convert(String value) throws Throwable {
        switch (value.length()) {
            case 5: return TIME_FORMATTER.parse(value);
            case 10: return DATE_FORMATTER.parse(value);
            case 16: return DATE_TIME_FORMATTER.parse(value);
            default: throw new IllegalArgumentException(String.format("Invalid date string -> %s", value));
        }
    }
    
    public static DateTimeConverter getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DateTimeConverter();
        
        return INSTANCE;
    }
    
}
