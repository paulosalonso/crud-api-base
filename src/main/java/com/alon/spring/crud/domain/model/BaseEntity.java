package com.alon.spring.crud.domain.model;

import java.io.Serializable;

public interface BaseEntity<T> extends Serializable {
    public T id();
}
