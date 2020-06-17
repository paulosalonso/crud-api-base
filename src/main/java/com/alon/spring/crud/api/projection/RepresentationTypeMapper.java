package com.alon.spring.crud.api.projection;

import java.lang.reflect.ParameterizedType;

public interface RepresentationTypeMapper<T> {
    String map();

    default Class<T> getMappedType() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericInterfaces()[0];
        return (Class<T>) type.getActualTypeArguments()[0];
    }
}
