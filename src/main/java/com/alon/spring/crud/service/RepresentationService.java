package com.alon.spring.crud.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RepresentationService {
    
    private final List<Class> FINAL_TYPES = List.of(
        Character.class, char.class,
        CharSequence.class,
        String.class,
        Integer.class, int.class,
        Short.class, short.class,
        Long.class, long.class,
        Float.class, float.class,
        Double.class, double.class,
        BigDecimal.class,
        Boolean.class, boolean.class,
        Byte.class, byte.class,
        Date.class,
        Calendar.class
    );
    
    public Map<String, Object> getRepresentationOf(Class clazz) {
        
        Map<String, Object> response = new HashMap<>();
        
        List.of(clazz.getDeclaredFields())
                .forEach(field -> response.put(field.getName(), this.mapField(field)));
        
        return response;
        
    }
    
    private Object mapField(Field field) {
        
        if (this.FINAL_TYPES.contains(field.getType()))
            return field.getType().getSimpleName();
        else if (Collection.class.isAssignableFrom(field.getType()))
            return this.mapCollection(field.getGenericType());
        
        Map<String, Object> response = new HashMap<>();
        
        List<Field> fields = List.of(field.getType().getDeclaredFields());
        
        fields.forEach(childField -> {
            
            if (Collection.class.isAssignableFrom(childField.getType()))
                response.put(childField.getName(), this.mapCollection(childField.getGenericType()));
            else
                response.put(childField.getName(), this.mapField(childField));
            
        });
        
        return response;
        
    }
    
    private Object mapCollection(Type type) {
        
        Class listItemsType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        
        return getRepresentationOf(listItemsType);
        
    }
    
}
