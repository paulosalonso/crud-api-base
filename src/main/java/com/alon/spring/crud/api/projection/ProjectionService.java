package com.alon.spring.crud.api.projection;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.exception.ProjectionException;

@Service
public class ProjectionService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionService.class);
    
    public static final String NOP_PROJECTION = "no-operation-projection";
    
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
        if (projectionName == null || projectionName.equals(NOP_PROJECTION))
            return (O) input;

        try {
            Projector projector = getProjector(projectionName);
            return (O) projector.project(input);
        } catch (ProjectionException e) {
            throw e;
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting entity %s with projector '%s'", 
                    input.getClass().getSimpleName(), 
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(message, e);
        }
    }

    public <I extends BaseEntity, O> OutputPage<O> project(String projectionName, Page<I> input) {
        try {
            List content = input.getContent();

            if (projectionName == null || projectionName.equals(NOP_PROJECTION)) {
                content = input.getContent();
            } else {
                Projector<I, O> projector = getProjector(projectionName);

                content = input.getContent().stream()
                        .map(projector::project)
                        .collect(Collectors.toList());
            }

            return OutputPage.of()
                    .page(input.getNumber())
                    .pageSize(input.getNumberOfElements())
                    .totalPages(input.getTotalPages())
                    .totalSize(Long.valueOf(input.getTotalElements()).intValue())
                    .content(content)
                    .build();
        } catch (ProjectionException e) {
            throw e;
        } catch (Exception e) {
            String message = String.format(
                    "Error projecting page with projector '%s'",
                    projectionName);
            
            LOGGER.error(message, e);
            throw new ProjectionException(message, e);
        }
    }
    
    public List<String> getRequiredExpand(String projectionName) {
        Projector projector = getProjector(projectionName);
        return projector.requiredExpand();
    }
    
    public boolean projectionExists(String projectionName) {
        return projections.containsKey(projectionName);
    }
    
    public List<ProjectionRepresentation> getEntityRepresentations(Class<? extends BaseEntity> clazz) {
        if (representationsCache.containsKey(clazz))
            return representationsCache.get(clazz);
        
        Map<String, Projector> projections = getEntityProjections(clazz);

        List<ProjectionRepresentation> representations = projections.entrySet().stream()
                .map(this::getProjectionRepresentation)
                .collect(Collectors.toList());
        
        representationsCache.put(clazz, representations);
        
        return representations;
    }

    private Projector getProjector(String projectionName) {
        Projector projector = projections.get(projectionName);

        if (projector == null)
            throw new ProjectionException(String.format(
                    "Projection '%s' not found", projectionName));

        return projector;
    }

    private Map<String, Projector> getEntityProjections(Class<? extends BaseEntity> entityType) {
        return projections.keySet().stream()
                .filter(key -> projectorInputTypeIsTheExpected(key, entityType))
                .collect(Collectors.toMap(projectionName -> projectionName, projections::get));
    }

    private ProjectionRepresentation getProjectionRepresentation(Map.Entry<String, Projector> entry) {
        Class projectionOutput = getProjectorOutputType(entry.getValue());
        Map<String, Object> representation = representationService.getRepresentationOf(projectionOutput);
        return new ProjectionRepresentation(entry.getKey(), representation);
    }

    private boolean projectorInputTypeIsTheExpected(String projectionName, Class<? extends BaseEntity> expectedInputType) {
        Projector projector = projections.get(projectionName);
        Type inputType = getProjectorInputType(projector);
        
        if (!(inputType instanceof Class)) // A classe pode usar generics e aqui seria um TypeVariableImpl ao invés de Class
            return false;
        
        return ((Class) inputType).isAssignableFrom(expectedInputType);
    }
    
    private Type getProjectorInputType(Projector projector) {
        ParameterizedType projectorType = getProjectorParameterizedType(projector);
        return projectorType.getActualTypeArguments()[0];
    }
    
    private Class getProjectorOutputType(Projector projector) {
        ParameterizedType projectorType = getProjectorParameterizedType(projector);
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

}
