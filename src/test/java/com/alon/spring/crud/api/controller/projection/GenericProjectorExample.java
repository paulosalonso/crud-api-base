package com.alon.spring.crud.api.controller.projection;

import com.alon.spring.crud.api.projection.Projector;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

public class GenericProjectorExample<I, O> implements Projector<I, O> {
    @Override
    public O project(I input) throws ProjectionException {
        return (O) input;
    }
}
