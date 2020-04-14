package com.alon.spring.crud.api.controller.projection;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.api.controller.output.EntityTestDTO;
import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.EntityTest;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

@Component("entityTestProjection")
public class EntityTestProjector implements Projector<EntityTest, EntityTestDTO> {
    @Override
    public EntityTestDTO project(EntityTest input) throws ProjectionException {
        return EntityTestDTO.of()
                .id(input.getId())
                .property(input.getStringProperty())
                .build();
    }

    @Override
    public List<String> requiredExpand() {
        return List.of("property");
    }
}
