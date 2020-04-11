package com.alon.spring.crud.api.projection;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.domain.model.BaseEntity;

@Component(ProjectionService.ENTITY_PROJECTION)
public class EntityProjector<I extends BaseEntity> implements Projector<I, I> {

    @Override
    public I project(I input) {
        return input;
    }
    
}
