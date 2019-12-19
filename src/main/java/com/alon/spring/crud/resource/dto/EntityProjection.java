package com.alon.spring.crud.resource.dto;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alon.spring.crud.model.BaseEntity;

@Component("default")
public class EntityProjection<I extends BaseEntity> implements Projection<I, I> {

    @Override
    public I project(I input) {
        return input;
    }
    
}
