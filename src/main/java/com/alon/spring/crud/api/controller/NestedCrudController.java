package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.Options;
import com.alon.spring.crud.api.controller.input.OptionsNormalizer;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.api.controller.input.SearchResolver;
import com.alon.spring.crud.api.controller.input.mapper.InputMapper;
import com.alon.spring.crud.api.controller.input.mapper.ModelMapperInputMapper;
import com.alon.spring.crud.api.projection.ProjectionRepresentation;
import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.NestedCrudService;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.alon.spring.crud.domain.service.exception.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class NestedCrudController<
        MASTER_ENTITY_ID_TYPE extends Serializable,
        MASTER_ENTITY_TYPE extends BaseEntity<MASTER_ENTITY_ID_TYPE>,
        MASTER_SERVICE_TYPE extends CrudService<MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE, ?>,
        NESTED_ENTITY_ID_TYPE extends Serializable,
        NESTED_ENTITY_TYPE extends BaseEntity<NESTED_ENTITY_ID_TYPE>,
        NESTED_SERVICE_TYPE extends NestedCrudService<
                MASTER_ENTITY_ID_TYPE, MASTER_ENTITY_TYPE,
                NESTED_ENTITY_ID_TYPE, NESTED_ENTITY_TYPE>,
        CREATE_INPUT_TYPE, UPDATE_INPUT_TYPE, SEARCH_INPUT_TYPE extends SearchInput> {

    protected final MASTER_SERVICE_TYPE masterService;
    protected final NESTED_SERVICE_TYPE nestedService;

    @Autowired
    protected ProjectionService projectionService;

    @Autowired
    private OptionsNormalizer optionsNormalizer;

    @Autowired
    private SearchResolver searchResolver;

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

    @ApiOperation(value = "Search nested resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping
    public ResponseEntity search(
            SEARCH_INPUT_TYPE search,
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
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

        Collection response;

        try {
            response = nestedService.search(masterId, criteria);
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        try {
            response = projectionService.project(options.getProjection(), response);
        } catch (ProjectionException e) {
            if (optionsNormalizer.projectDefaultOnError(options.getProjection(), this::getCollectionDefaultProjection))
                response = projectionService.project(getCollectionDefaultProjection(), response);
            else
                throw e;
        }

        return buildHttpGETResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @ApiOperation(value = "Read a nested resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{nestedId}")
    public ResponseEntity read(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @PathVariable NESTED_ENTITY_ID_TYPE nestedId,
            @Valid Options options,
            ServletWebRequest request
    ) throws ReadException {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        optionsNormalizer.normalizeOptions(options,
                this::getSingleDefaultProjection, this::getSingleAllowedProjections);

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
            if (optionsNormalizer.projectDefaultOnError(options.getProjection(), this::getSingleDefaultProjection))
                response = projectionService.project(getSingleDefaultProjection(), entity);
            else
                throw e;
        }

        return buildHttpGETResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @ApiOperation(value = "Create a nested resource",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping
    protected ResponseEntity create(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @RequestBody @Valid CREATE_INPUT_TYPE input) throws CreateException {

        NESTED_ENTITY_TYPE nestedEntity = createInputMapper.map(input);
        
        nestedEntity = nestedService.create(masterId, nestedEntity);
        
        Object response = projectionService.project(getSingleDefaultProjection(), nestedEntity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        
    }

    @ApiOperation(value = "Update a nested resource",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/{nestedId}")
    public ResponseEntity update(
            @PathVariable MASTER_ENTITY_ID_TYPE masterId,
            @PathVariable NESTED_ENTITY_ID_TYPE nestedId,
            @RequestBody @Valid UPDATE_INPUT_TYPE input) throws UpdateException {

        NESTED_ENTITY_TYPE entity;

        try {
            entity = nestedService.read(masterId, nestedId, Collections.emptyList());
        } catch (NotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        updateInputMapper.map(input, entity);
        entity = nestedService.update(masterId, nestedId, entity);

        Object response = projectionService.project(getSingleDefaultProjection(), entity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
        
    }

    @ApiOperation(value = "Delete a nested resource")
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

    @ApiOperation(value = "Get available nested resource projections and their representations",
            nickname = "Get projections", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/projections")
    public List<ProjectionRepresentation> getRepresentations() {
        return projectionService.getEntityRepresentations(extractNestedEntityType(),
                this::getSingleDefaultProjection, this::getCollectionDefaultProjection);
    }

    public BodyBuilder buildHttpGETResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status);
    }

    protected String getSingleDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    public List<String> getSingleAllowedProjections() {
        return Collections.emptyList();
    }

    protected String getCollectionDefaultProjection() {
        return ProjectionService.NOP_PROJECTION;
    }

    public List<String> getCollectionAllowedProjections() {
        return Collections.emptyList();
    }

    private final <T extends BaseEntity<NESTED_ENTITY_ID_TYPE>> Class<T> extractNestedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) classType.getActualTypeArguments()[4];
    }

}
