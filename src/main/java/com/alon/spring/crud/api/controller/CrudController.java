package com.alon.spring.crud.api.controller;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import com.alon.spring.crud.api.controller.input.EntityInputMapper;
import com.alon.spring.crud.api.controller.input.InputMapper;
import com.alon.spring.crud.api.controller.input.SearchInput;
import com.alon.spring.crud.api.controller.output.OutputPage;
import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.CrudService;
import com.alon.spring.crud.domain.service.ProjectionService;
import com.alon.spring.crud.domain.service.SearchCriteria;
import com.alon.spring.crud.domain.service.exception.CreateException;
import com.alon.spring.crud.domain.service.exception.DeleteException;
import com.alon.spring.crud.domain.service.exception.ReadException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import com.alon.spring.specification.ExpressionSpecification;

public abstract class CrudController<
        MANAGED_ENTITY_TYPE extends BaseEntity, 
        CREATE_INPUT_TYPE, 
        UPDATE_INPUT_TYPE,
        SEARCH_INPUT_TYPE extends SearchInput,
        SERVICE_TYPE extends CrudService
> {
	
    protected final SERVICE_TYPE service;
    
    protected ProjectionService projectionService;
    
    private final Class<MANAGED_ENTITY_TYPE> managedEntityClass;
    
    private InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper = new EntityInputMapper();
    private InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper = new EntityInputMapper();
    
    @Value("${com.alon.spring.crud.search.filter.expression.enabled:false}") 
    protected boolean enableSearchByExpression;
    
    public CrudController(SERVICE_TYPE service, ProjectionService projectionService) {
        this.service = service;
        this.projectionService = projectionService;
        this.managedEntityClass = this.extractManagedEntityTypeClass();
    }
    
    protected String getDefaultProjection() {
        return ProjectionService.ENTITY_PROJECTION;
    }

    protected final void setCreateInputMapper(InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper) {
        this.createInputMapper = createInputMapper;
    }

    protected final void setUpdateInputMapper(InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper) {
        this.updateInputMapper = updateInputMapper;
    }

    @GetMapping("${com.alon.spring.crud.path.search:}")
    public OutputPage search(
            SEARCH_INPUT_TYPE filter,
            @RequestParam(required = false) List<String> order,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            @RequestParam(required = false) List<String> expand,
            @RequestParam(required = false) String projection
    ) {
        
        String normalizedProjection = this.normalizeProjection(projection);
        expand = this.normalizeExpand(normalizedProjection, expand);

        SearchCriteria criteria = SearchCriteria.of()
                .filter(filter.toSpecification())
                .order(order)
                .page(this.normalizePage(page))
                .pageSize(pageSize)
                .expand(expand)
                .build();

        Page<MANAGED_ENTITY_TYPE> entities = this.service.search(criteria);
        
        return this.projectionService.project(normalizedProjection, entities);
        
    }
    
    @GetMapping("${com.alon.spring.crud.path.search:}/by-expression")
    public OutputPage search(
            @RequestParam(required = false) Optional<String> filter,
            @RequestParam(required = false) List<String> order,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "100") Integer pageSize,
            @RequestParam(required = false) List<String> expand,
            @RequestParam(required = false) String projection
    ) {
        
        if (!enableSearchByExpression)
            throw new ResponseStatusException(HttpStatus.LOCKED);
        
        String normalizedProjection = this.normalizeProjection(projection);
        expand = this.normalizeExpand(normalizedProjection, expand);
        
        Specification specification = null;
        
        if (filter.isPresent())
            specification = ExpressionSpecification.of(filter.get());

        SearchCriteria criteria = SearchCriteria.of()
                .filter(specification)
                .order(order)
                .page(this.normalizePage(page))
                .pageSize(pageSize)
                .expand(expand)
                .build();

        Page<MANAGED_ENTITY_TYPE> entities = this.service.search(criteria);
        
        return this.projectionService.project(normalizedProjection, entities);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read:/{id}}")
    public Object read(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> expand,
            @RequestParam(required = false) String projection
    ) throws ReadException {
        
        String normalizedProjection = this.normalizeProjection(projection);
        List<String> normalizedExpand = this.normalizeExpand(normalizedProjection, expand);
        
        MANAGED_ENTITY_TYPE entity = (MANAGED_ENTITY_TYPE) this.service.read(id, normalizedExpand);
        
        if (entity == null)
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        
        return this.projectionService.project(normalizedProjection, entity);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create:}")
    @ResponseStatus(HttpStatus.CREATED)
    protected Object create(
            @RequestBody @Valid CREATE_INPUT_TYPE input,
            @RequestParam(required = false) String projection
    ) throws CreateException {
        
        MANAGED_ENTITY_TYPE entity = (MANAGED_ENTITY_TYPE) this.createInputMapper.map(input);
        
        entity = (MANAGED_ENTITY_TYPE) this.service.create(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        return this.projectionService.project(normalizedProjection, entity);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update:}")
    public Object update(
            @RequestBody @Valid UPDATE_INPUT_TYPE input,
            @RequestParam(required = false) String projection
    ) throws UpdateException {
        
        MANAGED_ENTITY_TYPE entity = (MANAGED_ENTITY_TYPE) this.updateInputMapper.map(input);

        entity = (MANAGED_ENTITY_TYPE) this.service.update(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        return this.projectionService.project(normalizedProjection, entity);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete:/{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws DeleteException {
        this.service.delete(id);
    }
    
    @GetMapping("/projections")
    public List<ProjectionService.ProjectionRepresentation> getProjections() {
        
        return this.projectionService
                .getEntityRepresentations(this.managedEntityClass);
        
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
    
    private final Class extractManagedEntityTypeClass() {
        
         return (Class) List.of(((ParameterizedType) this.getClass().getGenericSuperclass())
                 .getActualTypeArguments()).get(0);
        
    }

}
