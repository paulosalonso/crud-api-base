package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static java.util.stream.Collectors.toMap;

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
            Byte.class, byte.class);

    private List<RepresentationTypeMapper> typeMappers;

    public RepresentationService(List<RepresentationTypeMapper> typeMappers) {
        this.typeMappers = typeMappers;
    }

    public Map<String, Object> getRepresentationOf(Class clazz) {
        List<Field> fields = List.of(clazz.getDeclaredFields());

        List<Type> parents = new ArrayList<>();
        parents.add(clazz);

        return Map.of(getTypeName(clazz), mapFields(fields, parents));
    }

    private Map<String, Object> mapFields(List<Field> fields, List<Type> parents) {
        return fields.stream()
                .collect(toMap(this::getFieldName, field -> mapField(field, parents)));
    }
    
    private Object mapField(Field field, List<Type> parents) {
        
        if (isFinalType(field)) {
            if (isArray(field))
                return field.getType().getComponentType().getSimpleName().toLowerCase();

            return field.getType().getSimpleName().toLowerCase();
        } else if (hasMapper(field)) {
            return getMapper(field).map();
        } else if (isCollection(field)) {
            return mapCollection(field, parents);
        } else if (parents.contains(field.getType())) {
            return getTypeName(field.getType());
        }

        parents.add(field.getType());

        List<Field> fields;

        if (isArray(field))
            fields = List.of(field.getType().getComponentType().getDeclaredFields());
        else
            fields = List.of(field.getType().getDeclaredFields());

        return Map.of(getTypeName(field.getType()), mapFields(fields, parents));

    }

    private Object mapCollection(Field field, List<Type> parents) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class collectionItemsType = (Class) type.getActualTypeArguments()[0];

        if (isFinalType(collectionItemsType))
            return collectionItemsType.getSimpleName().toLowerCase();
        else if (parents.contains(collectionItemsType))
            return getTypeName(collectionItemsType);

        parents.add(collectionItemsType);

        List<Field> fields = List.of(collectionItemsType.getDeclaredFields());

        return Map.of(getTypeName(collectionItemsType), mapFields(fields, parents));
    }

    private String getFieldName(Field field) {
        if (isArray(field) || isCollection(field))
            return String.format("%s[]", field.getName());

        return field.getName();
    }

    private String getTypeName(Type type) {
        String typeName = type.getTypeName();
        typeName = typeName.substring(typeName.lastIndexOf(".") + 1);

        if (typeName.endsWith("DTO"))
            return typeName.substring(0, typeName.length() - 3);

        return typeName;
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

    private boolean isArray(Field field) {
        return field.getType().isArray();
    }

    private boolean hasMapper(Field field) {
        return typeMappers.stream()
                .filter(mapper -> mapper.getMappedType().equals(field.getType()))
                .findFirst()
                .isPresent();
    }

    private RepresentationTypeMapper getMapper(Field field) {
        return typeMappers.stream()
                .filter(mapper -> mapper.getMappedType().equals(field.getType()))
                .findFirst()
                .get();
    }
    
}
