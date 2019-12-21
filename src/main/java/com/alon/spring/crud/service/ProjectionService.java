package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.projection.EntityProjection;
import com.alon.spring.crud.resource.projection.ListOutput;
import com.alon.spring.crud.resource.projection.Projection;
import com.alon.spring.crud.service.exception.ProjectionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectionService {

    @Autowired
    private ApplicationContext applicationContext;

    public <I extends BaseEntity, O> O project(I input, List<String> expandedFields) {
        return this.project("default", input, expandedFields);
    }

    public <I extends BaseEntity, O> O project(String projectionName, I input, List<String> expandedFields) {
        try {
            Projection projection = this.getProjection(projectionName);
            
            validateExpandRequeriment(projectionName, projection, expandedFields);
            
            return (O) projection.project(input);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            throw new ProjectionException(message, e);
        }
    }

    public <I extends BaseEntity> ListOutput project(Page<I> input, List<String> expandedFields) {
        return this.project("default", input, expandedFields);
    }

    public <I extends BaseEntity> ListOutput project(String projectionName, Page<I> input, List<String> expandedFields) {

        try {
            Projection projection = this.getProjection(projectionName);
            
            validateExpandRequeriment(projectionName, projection, expandedFields);
            
            return ListOutput.of(input, projection);
        } catch (Exception e) {
            throw new ProjectionException(e);
        }

    }

    private Projection getProjection(String projectionName) {

        Projection projection = this.applicationContext.getBeansOfType(Projection.class).get(projectionName);

        return Optional.ofNullable(projection)
                       .orElse(this.applicationContext.getBean(EntityProjection.class));

    }
    
    private void validateExpandRequeriment(String projectionName, Projection projection, List<String> expandedFields) {
        if (!expandedFields.containsAll(projection.expandDepends())) {            
            String message = String.format(
                    "%s projection requires follow expanded fields: %s, but received: %s",
                    projectionName,
                    projection.expandDepends(),
                    expandedFields
            );
            
            throw new ProjectionException(message);
        }
    }

}
