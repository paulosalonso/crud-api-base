package com.alon.spring.crud.resource.dto;

import com.alon.spring.crud.model.BaseEntity;

public class EntityOutputConverter<I extends BaseEntity> implements OutputDtoConverter<I, I> {

    @Override
    public I convert(I data) {
        return data;
    }
    
}
