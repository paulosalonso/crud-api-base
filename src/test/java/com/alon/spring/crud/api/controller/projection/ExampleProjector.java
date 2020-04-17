package com.alon.spring.crud.api.controller.projection;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.api.controller.output.ExampleDTO;
import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

@Component("exampleProjection")
public class ExampleProjector implements Projector<Example, ExampleDTO> {
    @Override
    public ExampleDTO project(Example input) throws ProjectionException {
        return ExampleDTO.of()
                .id(input.getId())
                .property(input.getStringProperty())
                .build();
    }

    @Override
    public List<String> requiredExpand() {
        return List.of("property");
    }
}
