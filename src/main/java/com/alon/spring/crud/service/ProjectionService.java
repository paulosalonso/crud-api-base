package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.projection.OutputPage;
import com.alon.spring.crud.resource.projection.Projector;
import com.alon.spring.crud.service.exception.ProjectionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    
    private RepresentationService representationService;
    
    private final Map<String, Projector> projections;
    
    private final Map<Class, List<ProjectionRepresentation>> representationsCache = new HashMap<>();
    
    public ProjectionService(ApplicationContext applicationContext, RepresentationService representationService) {
        this.applicationContext = applicationContext;
        this.representationService = representationService;
        this.projections = this.applicationContext.getBeansOfType(Projector.class);
    }

    public <I extends BaseEntity, O> O project(String projectionName, I input) {
        
        if (projectionName.equals(ENTITY_PROJECTION))
            return (O) input;
        
        try {
            Projector projector = this.getProjector(projectionName);
            
            return (O) projector.project(input);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(message, e);
        }
        
    }

    public <I extends BaseEntity> OutputPage project(String projectionName, Page<I> input) {

        try {
            Projector projector = this.getProjector(projectionName);
            return OutputPage.of(input, projector);
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(e);
        }

    }
    
    public List<String> getRequiredExpand(String projectionName) {
        
        Projector projector = this.projections.get(projectionName);
        
        if (projector == null)
            return Collections.emptyList();
        
        return projector.requiredExpand();
        
    }
    
    public boolean projectionExists(String projectionName) {
        return this.projections.containsKey(projectionName);
    }

    private Projector getProjector(String projectionName) {

        Projector projector = this.projections.get(projectionName);

        return Optional.ofNullable(projector)
                       .orElse(this.projections.get(ENTITY_PROJECTION));

    }

    private Map<String, Projector> getEntityProjections(Class<? extends BaseEntity> entityType) {

        return this.projections
                .keySet().stream()
                .filter(key -> this.projectorInputTypeIsTheExpected(key, entityType))
                .collect(Collectors.toMap(projectionName -> projectionName, this.projections::get));

    }
    
    public List<ProjectionRepresentation> getEntityRepresentations(Class<? extends BaseEntity> clazz) {
        
        if (this.representationsCache.containsKey(clazz))
            return this.representationsCache.get(clazz);
        
        Map<String, Projector> projections = this.getEntityProjections(clazz);

        return projections.entrySet().stream()
                .map(this::getProjectionRepresentation)
                .collect(Collectors.toList());

    }

    private ProjectionRepresentation getProjectionRepresentation(Map.Entry<String, Projector> entry) {
        Class projectionOutput = this.getProjectorOutputType(entry.getValue());
        Map<String, Object> representation = this.representationService.getRepresentationOf(projectionOutput);
        return new ProjectionRepresentation(entry.getKey(), representation);
    }

    private boolean projectorInputTypeIsTheExpected(String projectionName, Class<? extends BaseEntity> expectedInputType) {
        
        Projector projector = this.projections.get(projectionName);
        Type inputType = this.getProjectorInputType(projector);
        
        if (!(inputType instanceof Class)) // A classe pode usar generics e aqui seria um TypeVariableImpl ao invés de Class
            return false;
        
        return ((Class) inputType).isAssignableFrom(expectedInputType);
        
    }
    
    private Type getProjectorInputType(Projector projector) {
        ParameterizedType projectorType = this.getProjectorParameterizedType(projector);        
        return projectorType.getActualTypeArguments()[0];
    }
    
    private Class getProjectorOutputType(Projector projector) {
        ParameterizedType projectorType = this.getProjectorParameterizedType(projector);
        return (Class) projectorType.getActualTypeArguments()[1];
    }
    
    private ParameterizedType getProjectorParameterizedType(Projector projector) {
        
        Optional<Type> projectorTypeOpt = List.of(projector.getClass().getGenericInterfaces())
                .stream()
                .filter(type -> type instanceof ParameterizedType)
                .filter(type -> ((ParameterizedType) type).getRawType().equals(Projector.class))
                .findFirst();
        
        return (ParameterizedType) projectorTypeOpt.get();
        
    }

    public class ProjectionRepresentation {

        public String projectionName;
        public Map<String, Object> representation;

        public ProjectionRepresentation() {}

        public ProjectionRepresentation(String projectionName, Map<String, Object> representation) {
            this.projectionName = projectionName;
            this.representation = representation;
        }

        public String getProjectionName() {
            return projectionName;
        }

        public void setProjectionName(String projectionName) {
            this.projectionName = projectionName;
        }

        public Map<String, Object> getRepresentation() {
            return representation;
        }

        public void setRepresentation(Map<String, Object> representation) {
            this.representation = representation;
        }

    }

}
