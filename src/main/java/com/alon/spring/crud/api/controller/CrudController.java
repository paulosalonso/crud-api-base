package com.alon.spring.crud.api.controller;

import com.alon.spring.crud.api.controller.input.EntityInputMapper;
import com.alon.spring.crud.api.controller.input.InputMapper;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.core.properties.Properties;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.ProjectionService;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.alon.spring.crud.domain.service.exception.CreateException;
import com.alon.spring.crud.domain.service.exception.DeleteException;
import com.alon.spring.crud.domain.service.exception.ReadException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import com.alon.spring.specification.ExpressionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

    protected boolean disableContentCaching = true;

    private Class<MANAGED_ENTITY_TYPE> managedEntityClass;
    
    public CrudController(SERVICE_TYPE service) {
        this(service, new EntityInputMapper(), new EntityInputMapper());
    }

    public CrudController(SERVICE_TYPE service, boolean disableContentCaching) {
        this(service, new EntityInputMapper(), new EntityInputMapper(), disableContentCaching);
    }
    
    protected CrudController(SERVICE_TYPE service, 
    		InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper,
    		InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper) {
    	
    	this.service = service;
        this.createInputMapper = createInputMapper;
        this.updateInputMapper = updateInputMapper;
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
            @RequestParam(required = false) List<String> order,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            @RequestParam(required = false) List<String> expand,
            @RequestParam(required = false) String projection,
            ServletWebRequest request
    ) {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());
        
        String normalizedProjection = this.normalizeProjection(projection);
        expand = this.normalizeExpand(normalizedProjection, expand);

        SearchCriteria criteria = SearchCriteria.of()
                .filter(resolveFilter(filter))
                .order(order)
                .page(this.normalizePage(page))
                .pageSize(pageSize)
                .expand(expand)
                .build();

        Page<MANAGED_ENTITY_TYPE> entities = this.service.search(criteria);
        
        OutputPage response = this.projectionService.project(normalizedProjection, entities);

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read:/{id}}")
    public ResponseEntity read(
            @PathVariable MANAGED_ENTITY_ID_TYPE id,
            @RequestParam(required = false) List<String> expand,
            @RequestParam(required = false) String projection,
            ServletWebRequest request
    ) throws ReadException {
        if (disableContentCaching)
            ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());
        
        String normalizedProjection = this.normalizeProjection(projection);
        List<String> normalizedExpand = this.normalizeExpand(normalizedProjection, expand);
        
        MANAGED_ENTITY_TYPE entity = this.service.read(id, normalizedExpand);
        
        if (entity == null)
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        
        Object response = this.projectionService.project(normalizedProjection, entity);

        return buildResponseEntity(HttpStatus.OK)
                .body(response);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create:}")
    protected ResponseEntity create(
            @RequestBody @Valid CREATE_INPUT_TYPE input,
            @RequestParam(required = false) String projection
    ) throws CreateException {
        
        MANAGED_ENTITY_TYPE entity = this.createInputMapper.map(input);
        
        entity = this.service.create(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        Object response = this.projectionService.project(normalizedProjection, entity);

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
        
        MANAGED_ENTITY_TYPE entity = this.updateInputMapper.map(input);
        entity.setId(id);

        entity = this.service.update(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        Object response = this.projectionService.project(normalizedProjection, entity);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete:/{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable MANAGED_ENTITY_ID_TYPE id) throws DeleteException {
        this.service.delete(id);
    }
    
    @GetMapping("/projections")
    public List<ProjectionService.ProjectionRepresentation> getRepresentations() {
        
        return this.projectionService
                .getEntityRepresentations(getManagedEntityType());
        
    }

    public BodyBuilder buildResponseEntity(HttpStatus status) {
        return ResponseEntity.status(status);
    }

    public final Class getManagedEntityType() {
        if (managedEntityClass == null)
            managedEntityClass = (Class) List.of(((ParameterizedType) this.getClass().getGenericSuperclass())
                    .getActualTypeArguments()).get(1);

        return managedEntityClass;
    }

    protected String getDefaultProjection() {
        return ProjectionService.ENTITY_PROJECTION;
    }
    
    protected int normalizePage(int page) {
        return --page;
    }
    
    protected String normalizeProjection(String projectionName) {
        if (projectionName != null && this.projectionService.projectionExists(projectionName))
            return projectionName;
        
        return this.getDefaultProjection();
    }
    
    protected List<String> normalizeExpand(String projectionName, List<String> receivedExpand) {
        
        if (projectionName == null || projectionName.equals(ProjectionService.ENTITY_PROJECTION))
            return receivedExpand;
        
        return this.projectionService.getRequiredExpand(projectionName);
        
    }

    protected Specification resolveFilter(SEARCH_INPUT_TYPE filter) {

        if (filter.expressionPresent()) {
            if (!properties.search.enableExpressionFilter)
                throw new ResponseStatusException(HttpStatus.LOCKED,
                        "The filter by expression resource is not enabled.");

            return ExpressionSpecification.of(filter.getExpression());
        }

        return filter.toSpecification();

    }

}
