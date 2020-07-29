package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.Options;
import com.alon.spring.crud.api.controller.input.OptionsNormalizer;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.api.controller.input.SearchResolver;
import com.alon.spring.crud.api.controller.input.mapper.InputMapper;
import com.alon.spring.crud.api.controller.input.mapper.ModelMapperInputMapper;
import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.api.projection.ProjectionRepresentation;
import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.alon.spring.crud.domain.service.exception.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

public abstract class CrudController<
        MANAGED_ENTITY_ID_TYPE extends Serializable,
        MANAGED_ENTITY_TYPE extends BaseEntity<MANAGED_ENTITY_ID_TYPE>,
        CREATE_INPUT_TYPE, 
        UPDATE_INPUT_TYPE,
        SEARCH_INPUT_TYPE extends SearchInput,
        SERVICE_TYPE extends CrudService<MANAGED_ENTITY_ID_TYPE, MANAGED_ENTITY_TYPE, ?>
> {
	
    protected final SERVICE_TYPE service;
    
    @Autowired
    protected ProjectionService projectionService;

    @Autowired
    protected OptionsNormalizer optionsNormalizer;

    @Autowired
    private SearchResolver searchResolver;
    
    protected InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper;
    protected InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper;

    protected final boolean disableContentCaching;

    protected Class<MANAGED_ENTITY_TYPE> managedEntityClass = extractManagedEntityType();
    
    public CrudController(SERVICE_TYPE service) {
        this.service = service;
        this.createInputMapper = new ModelMapperInputMapper<>(managedEntityClass);
        this.updateInputMapper = new ModelMapperInputMapper<>(managedEntityClass);
        this.disableContentCaching = true;
    }

    public CrudController(SERVICE_TYPE service, boolean disableContentCaching) {
        this.service = service;
        this.createInputMapper = new ModelMapperInputMapper<>(managedEntityClass);
        this.updateInputMapper = new ModelMapperInputMapper<>(managedEntityClass);
        this.disableContentCaching = disableContentCaching;
    }
    
    protected CrudController(SERVICE_TYPE service, 
    		InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper,
    		InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper) {

        this(service, createInputMapper, updateInputMapper, true);
    }

    protected CrudController(SERVICE_TYPE service,
             InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper,
             InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper,
             boolean disableContentCaching) {

        this.service = service;
        this.createInputMapper = createInputMapper;
        this.updateInputMapper = updateInputMapper;
        this.disableContentCaching = disableContentCaching;
    }

    @ApiOperation(value = "Search resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping
    public ResponseEntity search(
            SEARCH_INPUT_TYPE search,
            Pageable pageable,
            @Valid Options options,
            ServletWebRequest request
    ) {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        optionsNormalizer.normalizeOptions(options,
                this::getCollectionDefaultProjection, this::getCollectionAllowedProjections);

        Specification specification = searchResolver.resolve(search);

        SearchCriteria criteria = SearchCriteria.of()
                .filter(specification)
                .pageable(pageable)
                .expand(options.getExpand())
                .build();

        Page<MANAGED_ENTITY_TYPE> page = service.search(criteria);

        OutputPage response;

        try {
            response = projectionService.project(options.getProjection(), page);
        } catch (ProjectionException e) {
            if (optionsNormalizer.projectDefaultOnError(options.getProjection(), this::getCollectionDefaultProjection))
                response = projectionService.project(getCollectionDefaultProjection(), page);
            else
                throw e;
        }

        return buildHttpGETResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @ApiOperation(value = "Read a resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{id}")
    public ResponseEntity read(
            @PathVariable MANAGED_ENTITY_ID_TYPE id,
            @Valid Options options,
            ServletWebRequest request
    ) throws ReadException {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        optionsNormalizer.normalizeOptions(options,
                this::getSingleDefaultProjection, this::getSingleAllowedProjections);

        MANAGED_ENTITY_TYPE entity;

        try {
            entity = service.read(id, List.copyOf(options.getExpand()));
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        Object response;

        try {
            response = projectionService.project(options.getProjection(), entity);
        } catch (ProjectionException e) {
            if (optionsNormalizer.projectDefaultOnError(options.getProjection(), this::getSingleDefaultProjection))
                response = projectionService.project(getSingleDefaultProjection(), entity);
            else
                throw e;
        }

        return buildHttpGETResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @ApiOperation(value = "Create a resource",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping
    protected ResponseEntity create(@RequestBody @Valid CREATE_INPUT_TYPE input) throws CreateException {
        MANAGED_ENTITY_TYPE entity = createInputMapper.map(input);
        
        entity = service.create(entity);
        
        Object response = projectionService.project(getSingleDefaultProjection(), entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        
    }

    @ApiOperation(value = "Update a resource",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/{id}")
    public ResponseEntity update(
            @PathVariable MANAGED_ENTITY_ID_TYPE id,
            @RequestBody @Valid UPDATE_INPUT_TYPE input) throws UpdateException {

        MANAGED_ENTITY_TYPE entity;

        try {
            entity = service.read(id);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        updateInputMapper.map(input, entity);

        entity = service.update(entity);

        Object response = projectionService.project(getSingleDefaultProjection(), entity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
        
    }

    @ApiOperation("Delete a resource")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MANAGED_ENTITY_ID_TYPE id) throws DeleteException {
        try {
            service.delete(id);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @ApiOperation(value = "Get available projections and their representations",
            nickname = "Get projections", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/projections")
    public List<ProjectionRepresentation> getRepresentations() {
        return projectionService.getEntityRepresentations(extractManagedEntityType(),
                this::getSingleDefaultProjection, this::getCollectionDefaultProjection);
    }

    public BodyBuilder buildHttpGETResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status);
    }

    protected String getSingleDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    protected List<String> getSingleAllowedProjections() {
        return Collections.emptyList();
    }

    protected String getCollectionDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    protected List<String> getCollectionAllowedProjections() {
        return Collections.emptyList();
    }

    private final <T extends BaseEntity<MANAGED_ENTITY_ID_TYPE>> Class<T> extractManagedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) classType.getActualTypeArguments()[1];
    }

}
