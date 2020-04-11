package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.mapper.InputMapper;
import com.alon.spring.crud.api.controller.input.mapper.ModelMapperInputMapper;
import com.alon.spring.crud.api.controller.input.Options;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.api.projection.ProjectionRepresentation;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.api.projection.ProjectionService;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.alon.spring.crud.domain.service.exception.CreateException;
import com.alon.spring.crud.domain.service.exception.DeleteException;
import com.alon.spring.crud.domain.service.exception.ReadException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import com.alon.spring.specification.ExpressionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
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
    protected Properties properties;
    
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
    	
    	this.service = service;
        this.createInputMapper = createInputMapper;
        this.updateInputMapper = updateInputMapper;
        this.disableContentCaching = true;
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

    @GetMapping("${com.alon.spring.crud.path.search:}")
    public ResponseEntity search(
            SEARCH_INPUT_TYPE filter,
            Pageable pageable,
            Options options,
            ServletWebRequest request
    ) {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        normalizeCollectionOptions(options);

        SearchCriteria criteria = SearchCriteria.of()
                .filter(resolveFilter(filter))
                .pageable(pageable)
                .expand(options.getExpand())
                .build();

        Page<MANAGED_ENTITY_TYPE> entities = service.search(criteria);

        OutputPage response = projectionService.project(options.getProjection(), entities);

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("${com.alon.spring.crud.path.read:/{id}}")
    public ResponseEntity read(
            @PathVariable MANAGED_ENTITY_ID_TYPE id,
            Options options,
            ServletWebRequest request
    ) throws ReadException {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        normalizeSingleOptions(options);

        MANAGED_ENTITY_TYPE entity = service.read(id, options.getExpand());

        if (entity == null)
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);

        Object response = projectionService.project(options.getProjection(), entity);

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("${com.alon.spring.crud.path.create:}")
    protected ResponseEntity create(
            @RequestBody @Valid CREATE_INPUT_TYPE input,
            @RequestParam(required = false) String projection
    ) throws CreateException {
        
        MANAGED_ENTITY_TYPE entity = createInputMapper.map(input);
        
        entity = service.create(entity);
        
        projection = normalizeSingleProjection(projection);
        
        Object response = projectionService.project(projection, entity);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update:/{id}}")
    public ResponseEntity update(
            @PathVariable MANAGED_ENTITY_ID_TYPE id,
            @RequestBody @Valid UPDATE_INPUT_TYPE input,
            @RequestParam(required = false) String projection
    ) throws UpdateException {
        
        MANAGED_ENTITY_TYPE entity = updateInputMapper.map(input);
        entity.setId(id);

        entity = service.update(entity);
        
        projection = normalizeSingleProjection(projection);
        
        Object response = projectionService.project(projection, entity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete:/{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MANAGED_ENTITY_ID_TYPE id) throws DeleteException {
        service.delete(id);
    }
    
    @GetMapping("/projections")
    public List<ProjectionRepresentation> getRepresentations() {
        return projectionService.getEntityRepresentations(extractManagedEntityType());
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

    protected void normalizeSingleOptions(Options options) {
        options.setProjection(normalizeSingleProjection(options.getProjection()));
        options.setExpand(normalizeExpand(options.getProjection(), options.getExpand()));
    }

    protected void normalizeCollectionOptions(Options options) {
        options.setProjection(normalizeCollectionProjection(options.getProjection()));
        options.setExpand(normalizeExpand(options.getProjection(), options.getExpand()));
    }

    protected String normalizeSingleProjection(String projectionName) {
        if (projectionName != null && projectionService.projectionExists(projectionName))
            return projectionName;

        return getSingleDefaultProjection();
    }

    protected String normalizeCollectionProjection(String projectionName) {
        if (projectionName != null && projectionService.projectionExists(projectionName))
            return projectionName;

        return getCollectionDefaultProjection();
    }

    protected List<String> normalizeExpand(String projectionName, List<String> receivedExpand) {

        if (projectionName.equals(ProjectionService.NOP_PROJECTION))
            return receivedExpand;

        return projectionService.getRequiredExpand(projectionName);

    }

    protected Specification resolveFilter(SEARCH_INPUT_TYPE filter) {
        if (filter.expressionPresent()) {
            if (!properties.search.enableExpressionFilter)
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "The filter by expression feature is not enabled.");

            return ExpressionSpecification.of(filter.getExpression());
        }

        return filter.toSpecification();
    }

    private final <T extends BaseEntity<MANAGED_ENTITY_ID_TYPE>> Class<T> extractManagedEntityType() {
        ParameterizedType classType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) classType.getActualTypeArguments()[1];
    }

}
