package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;
import org.springframework.stereotype.Component;

@Component
public class EntityInputMapper<I extends BaseEntity> implements InputMapper<I, I> {

    @Override
    public I convert(I input) {
        return input;
    }
    
}
