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
                .collect(Collectors.toMap(this::getFieldName, this::mapField));
        
    }

    private String getFieldName(Field field) {
        if (field.getType().isArray() || isCollection(field))
            return String.format("%s[]", field.getName());

        return field.getName();
    }
    
    private Object mapField(Field field) {
        
        if (isFinalType(field)) {
            if (field.getType().isArray())
                return field.getType().getComponentType().getSimpleName().toLowerCase();

            return field.getType().getSimpleName().toLowerCase();
        } else if (isCollection(field)) {
            return mapCollection(field);
        }

        List<Field> fields;

        if (field.getType().isArray())
            fields = List.of(field.getType().getComponentType().getDeclaredFields());
        else
            fields = List.of(field.getType().getDeclaredFields());

        return fields.stream()
                .collect(Collectors.toMap(this::getFieldName, this::mapField));

    }

    private Object mapCollection(Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class listItemsType = (Class) type.getActualTypeArguments()[0];

        if (isFinalType(listItemsType))
            return listItemsType.getSimpleName().toLowerCase();

        return getRepresentationOf(listItemsType);
    }

    private boolean isFinalType(Field field) {
        return isFinalType(field.getType());
    }

    private boolean isFinalType(Class clazz) {
        if (clazz.isArray())
            return FINAL_TYPES.contains(clazz.getComponentType());

        return FINAL_TYPES.contains(clazz);
    }

    private boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }
    
}
