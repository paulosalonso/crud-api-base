package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.projection.EntityProjection;
import com.alon.spring.crud.resource.projection.ListOutput;
import com.alon.spring.crud.resource.projection.Projection;
import com.alon.spring.crud.service.exception.ProjectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectionService {

    @Autowired
    private ApplicationContext applicationContext;

    public <I extends BaseEntity, O> O project(I input) {
        return this.project("default", input);
    }

    public <I extends BaseEntity, O> O project(String projectionName, I input) {
        try {
            return (O) this.getProjection(projectionName).project(input);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            throw new ProjectionException(message, e);
        }
    }

    public <I extends BaseEntity> ListOutput project(Page<I> input) {
        return this.project("default", input);
    }

    public <I extends BaseEntity> ListOutput project(String projectionName, Page<I> input) {

        try {
            return ListOutput.of(input, this.getProjection(projectionName));
        } catch (Exception e) {
            throw new ProjectionException(e);
        }

    }

    private Projection getProjection(String projectionName) {

        Projection projection = this.applicationContext.getBeansOfType(Projection.class).get(projectionName);

        return Optional.ofNullable(projection)
                       .orElse(this.applicationContext.getBean(EntityProjection.class));

    }

}
