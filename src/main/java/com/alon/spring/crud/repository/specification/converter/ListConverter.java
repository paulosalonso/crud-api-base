/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alon.spring.crud.repository.specification.converter;

import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Path;

/**
 *
 * @author paulo
 */
public class ListConverter implements DecoderConverter<List> {

    @Override
    public List<Comparable> convert(String value) throws Throwable {
        throw new UnsupportedOperationException("Method unavailable. Use convert(Path path, String value).");
    }
    
    public List<?> convert(Path<?> path, String value) throws Throwable {
        String[] values = value.split((","));
        
        List result = Collections.emptyList();
        
        for (String v : values)
            result.add(ConverterResolver.resolve(path.getJavaType()).convert(v));
        
        return result;
    }
    
}
