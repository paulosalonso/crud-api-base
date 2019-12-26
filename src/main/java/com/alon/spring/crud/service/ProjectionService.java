package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.projection.OutputPage;
import com.alon.spring.crud.resource.projection.Projection;
import com.alon.spring.crud.service.exception.ProjectionException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ProjectionService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionService.class);
    
    public static final String ENTITY_PROJECTION = "entity-projection";
    
    private ApplicationContext applicationContext;
    
    private final Map<String, Projection> projections;
    
    public ProjectionService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        
        this.projections = this.applicationContext.getBeansOfType(Projection.class);
    }

    public <I extends BaseEntity, O> O project(String projectionName, I input, List<String> expandedFields) {
        
        if (projectionName.equals(ENTITY_PROJECTION))
            return (O) input;
        
        try {
            Projection projection = this.getProjection(projectionName);
            
            return (O) projection.project(input);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(message, e);
        }
        
    }

    public <I extends BaseEntity> OutputPage project(String projectionName, Page<I> input, List<String> expandedFields) {

        try {
            Projection projection = this.getProjection(projectionName);
            return OutputPage.of(input, projection);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(e);
        }

    }

    private Projection getProjection(String projectionName) {

        Projection projection = this.projections.get(projectionName);

        return Optional.ofNullable(projection)
                       .orElse(this.projections.get(ENTITY_PROJECTION));

    }
    
    public List<String> getRequiredExpand(String projectionName) {
        
        Projection projection = this.projections.get(projectionName);
        
        if (projection == null)
            throw new IllegalArgumentException(
                    String.format("The projection %s not exists.", projectionName));
        
        return projection.requiredExpand();
        
    }
    
    public void validateExpandRequeriment(String projectionName, List<String> expandedFields) {
        
        Projection projection = this.getProjection(projectionName);
        
        if (!expandedFields.containsAll(projection.requiredExpand())) {            
            String message = String.format(
                    "%s projection requires follow expanded fields: %s, but received: %s",
                    projectionName,
                    projection.requiredExpand(),
                    expandedFields
            );
            
            throw new ProjectionException(message);    
        }
        
    }

}
