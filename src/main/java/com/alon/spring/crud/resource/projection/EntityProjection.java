package com.alon.spring.crud.resource.projection;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.service.ProjectionService;

@Component(ProjectionService.ENTITY_PROJECTION)
public class EntityProjection<I extends BaseEntity> implements Projector<I, I> {

    @Override
    public I project(I input) {
        return input;
    }
    
}
