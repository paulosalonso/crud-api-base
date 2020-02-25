package com.alon.spring.crud.api.controller.input;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.domain.model.BaseEntity;

@Component
public class EntityInputMapper<I extends BaseEntity> implements InputMapper<I, I> {

    @Override
    public I map(I input) {
        return input;
    }
    
}
