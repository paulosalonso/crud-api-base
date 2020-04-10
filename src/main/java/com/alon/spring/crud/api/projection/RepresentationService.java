package com.alon.spring.crud.api.projection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class RepresentationService {
    
    private static final List<Class> FINAL_TYPES = List.of(
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

        List<Field> fields = List.of(clazz.getDeclaredFields());

        return fields.stream()
                .collect(Collectors.toMap(Field::getName, this::mapField));
        
    }
    
    private Object mapField(Field field) {
        
        if (this.isFinalType(field))
            return field.getType().getSimpleName();
        else if (this.isCollection(field))
            return this.mapCollection(field);
        
        List<Field> fields = List.of(field.getType().getDeclaredFields());

        return fields.stream()
                .collect(Collectors.toMap(Field::getName, this::mapField));

    }

    private Object mapCollection(Field field) {

        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class listItemsType = (Class) type.getActualTypeArguments()[0];

        return getRepresentationOf(listItemsType);

    }

    private boolean isFinalType(Field field) {
        return FINAL_TYPES.contains(field.getType());
    }

    private boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }
    
}
