package com.alon.spring.crud.api.controller.projection;

import org.springframework.stereotype.Component;

import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.model.Example;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

@Component("errorExampleProjection")
public class ErrorExampleProjector implements Projector<Example, Example> {
    @Override
    public Example project(Example input) throws ProjectionException {
        throw new RuntimeException();
    }
}
