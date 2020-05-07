package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.Options;
import com.alon.spring.crud.api.controller.input.ProjectionOption;
import com.alon.spring.crud.api.controller.input.mapper.InputMapper;
import com.alon.spring.crud.api.controller.input.mapper.ModelMapperInputMapper;
import com.alon.spring.crud.api.projection.ProjectionRepresentation;
import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.model.NestedBaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.NestedCrudService;
import com.alon.spring.crud.domain.service.NestedOwnerNestedCrudService;
import com.alon.spring.crud.domain.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public abstract class NestedCrudController<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        MASTER_SERVICE_TYPE extends CrudService<MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE, ?>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>,
        NESTED_SERVICE_TYPE extends NestedCrudService<
                                MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE,
                                NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE>,
        CREATE_INPUT_TYPE,
        UPDATE_INPUT_TYPE> {

    protected final MASTER_SERVICE_TYPE masterService;
    protected final NESTED_SERVICE_TYPE nestedService;

    @Autowired
    protected ProjectionService projectionService;

    @Autowired
    protected Properties properties;

    protected InputMapper<CREATE_INPUT_TYPE, NESTED_ENTITY_TYPE> createInputMapper;
    protected InputMapper<UPDATE_INPUT_TYPE, NESTED_ENTITY_TYPE> updateInputMapper;

    protected final boolean disableContentCaching;

    protected Class<NESTED_ENTITY_TYPE> nestedEntityClass = extractNestedEntityType();

    public NestedCrudController(MASTER_SERVICE_TYPE masterService, NESTED_SERVICE_TYPE nestedService) {
        this.masterService = masterService;
        this.nestedService = nestedService;
        this.createInputMapper = new ModelMapperInputMapper<>(nestedEntityClass);
        this.updateInputMapper = new ModelMapperInputMapper<>(nestedEntityClass);
        this.disableContentCaching = true;
    }

    public NestedCrudController(MASTER_SERVICE_TYPE masterService,
            NESTED_SERVICE_TYPE nestedService, boolean disableContentCaching) {

        this.masterService = masterService;
        this.nestedService = nestedService;
        this.createInputMapper = new ModelMapperInputMapper<>(nestedEntityClass);
        this.updateInputMapper = new ModelMapperInputMapper<>(nestedEntityClass);
        this.disableContentCaching = disableContentCaching;
    }

    protected NestedCrudController(MASTER_SERVICE_TYPE masterService,
            NESTED_SERVICE_TYPE nestedService,
            InputMapper<CREATE_INPUT_TYPE, NESTED_ENTITY_TYPE> createInputMapper,
            InputMapper<UPDATE_INPUT_TYPE, NESTED_ENTITY_TYPE> updateInputMapper) {

        this(masterService, nestedService, createInputMapper, updateInputMapper, true);
    }

    protected NestedCrudController(MASTER_SERVICE_TYPE masterService,
            NESTED_SERVICE_TYPE nestedService,
            InputMapper<CREATE_INPUT_TYPE, NESTED_ENTITY_TYPE> createInputMapper,
            InputMapper<UPDATE_INPUT_TYPE, NESTED_ENTITY_TYPE> updateInputMapper,
            boolean disableContentCaching) {

        this.masterService = masterService;
        this.nestedService = nestedService;
        this.createInputMapper = createInputMapper;
        this.updateInputMapper = updateInputMapper;
        this.disableContentCaching = disableContentCaching;
    }

    @GetMapping
    public ResponseEntity getAll(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @Valid Options options,
            ServletWebRequest request
    ) {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        normalizeOptions(options, this::getCollectionDefaultProjection);

        Collection response;

        try {
            response = nestedService.getAll(masterId, List.copyOf(options.getExpand()));
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        try {
            response = projectionService.project(options.getProjection(), response);
        } catch (ProjectionException e) {
            if (projectDefaultOnError(options.getProjection(), this::getCollectionDefaultProjection))
                response = projectionService.project(getCollectionDefaultProjection(), response);
            else
                throw e;
        }

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{nestedId}")
    public ResponseEntity read(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @PathVariable NESTED_ENTITY_ID_TYPE nestedId,
            @Valid Options options,
            ServletWebRequest request
    ) throws ReadException {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        normalizeOptions(options, this::getSingleDefaultProjection);

        NESTED_ENTITY_TYPE entity;

        try {
            entity = nestedService.read(masterId, nestedId, List.copyOf(options.getExpand()));
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        Object response;

        try {
            response = projectionService.project(options.getProjection(), entity);
        } catch (ProjectionException e) {
            if (projectDefaultOnError(options.getProjection(), this::getSingleDefaultProjection))
                response = projectionService.project(getSingleDefaultProjection(), entity);
            else
                throw e;
        }

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @PostMapping
    protected ResponseEntity create(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @RequestBody @Valid CREATE_INPUT_TYPE input,
            @Valid ProjectionOption option
    ) throws CreateException {
        normalizeProjectionOption(option, this::getSingleDefaultProjection);

        NESTED_ENTITY_TYPE nestedEntity = createInputMapper.map(input);
        
        nestedEntity = nestedService.create(masterId, nestedEntity);
        
        Object response;

        try {
            response = projectionService.project(option.getProjection(), nestedEntity);
        } catch (ProjectionException e) {
            if (projectDefaultOnError(option.getProjection(), this::getSingleDefaultProjection))
                response = projectionService.project(getSingleDefaultProjection(), nestedEntity);
            else
                throw e;
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        
    }

    @PutMapping("/{nestedId}")
    public ResponseEntity update(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @PathVariable NESTED_ENTITY_ID_TYPE nestedId,
            @RequestBody @Valid UPDATE_INPUT_TYPE input,
            @Valid ProjectionOption option
    ) throws UpdateException {
        normalizeProjectionOption(option, this::getSingleDefaultProjection);

        NESTED_ENTITY_TYPE entity = updateInputMapper.map(input);
        entity.setId(nestedId);

        entity = nestedService.update(masterId, nestedId, entity);

        Object response;

        try {
            response = projectionService.project(option.getProjection(), entity);
        } catch (ProjectionException e) {
            if (projectDefaultOnError(option.getProjection(), this::getSingleDefaultProjection))
                response = projectionService.project(getSingleDefaultProjection(), entity);
            else
                throw e;
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
        
    }

    @DeleteMapping("/{nestedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MASTER_ENTITY_ID_TYPE masterId,
           @PathVariable NESTED_ENTITY_ID_TYPE nestedId) throws DeleteException {

        try {
            nestedService.delete(masterId, nestedId);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
    
    @GetMapping("/projections")
    public List<ProjectionRepresentation> getRepresentations() {
        return projectionService.getEntityRepresentations(extractNestedEntityType());
    }

    public BodyBuilder buildResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status);
    }

    protected String getSingleDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    protected String getCollectionDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    protected void normalizeOptions(Options options, Supplier<String> defaultProjectionSupplier) {
        if (options.getProjection() == null)
            options.setProjection(defaultProjectionSupplier.get());

        if (!options.getProjection().equals(ProjectionService.NOP_PROJECTION))
            try {
                if (options.getExpand() != null)
                    options.getExpand().addAll(projectionService.getRequiredExpand(options.getProjection()));
                else
                    options.setExpand(projectionService.getRequiredExpand(options.getProjection()));
            } catch (ProjectionException e) {
                // NOP
            }
    }

    protected void normalizeProjectionOption(ProjectionOption option, Supplier<String> defaultProjectionSupplier) {
        if (option.getProjection() == null)
            option.setProjection(defaultProjectionSupplier.get());
    }

    private final <T extends BaseEntity<NESTED_ENTITY_ID_TYPE>> Class<T> extractNestedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) classType.getActualTypeArguments()[4];
    }

    private boolean projectDefaultOnError(String projection, Supplier<String> defaultProjectionSupplier) {
        return properties.projection.useDefaultIfError
                && !projection.equals(defaultProjectionSupplier.get());
    }

}
