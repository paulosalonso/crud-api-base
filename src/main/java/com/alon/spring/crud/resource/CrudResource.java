package com.alon.spring.crud.resource;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.input.EntityInputMapper;
import com.alon.spring.crud.resource.input.InputMapper;
import com.alon.spring.crud.resource.input.SearchInput;
import com.alon.spring.crud.resource.projection.OutputPage;
import com.alon.spring.crud.service.CrudService;
import com.alon.spring.crud.service.ProjectionService;
import com.alon.spring.crud.service.RepresentationService;
import com.alon.spring.crud.service.SearchCriteria;
import com.alon.spring.crud.service.exception.CreateException;
import com.alon.spring.crud.service.exception.DeleteException;
import com.alon.spring.crud.service.exception.ReadException;
import com.alon.spring.crud.service.exception.UpdateException;
import com.alon.spring.specification.ExpressionSpecification;

import java.lang.reflect.ParameterizedType;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

public abstract class CrudResource<
        MANAGED_ENTITY_TYPE extends BaseEntity, 
        CREATE_INPUT_TYPE, 
        UPDATE_INPUT_TYPE,
        SEARCH_INPUT_TYPE extends SearchInput,
        SERVICE_TYPE extends CrudService
> {
	
    protected final SERVICE_TYPE service;
    
    @Autowired
    protected ProjectionService projectionService;
    
    @Autowired
    private RepresentationService representationService;
    
    private final Class<MANAGED_ENTITY_TYPE> managedEntityClass;
    
    private InputMapper<CREATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> createInputMapper = new EntityInputMapper();
    private InputMapper<UPDATE_INPUT_TYPE, MANAGED_ENTITY_TYPE> updateInputMapper = new EntityInputMapper();
    
    @Value("${com.alon.spring.crud.search.filter.expression.enabled:false}") 
    protected boolean enableSearchByExpression;
    
    public CrudResource(SERVICE_TYPE service) {
        this.service = service;
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
            @RequestParam(value = "order", required = false) List<String> order,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "100") Integer pageSize,
            @RequestParam(value = "expand", required = false) List<String> expand,
            @RequestParam(value = "projection", required = false) String projection
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
            @RequestParam(value = "filter") Optional<String> filter,
            @RequestParam(value = "order", required = false) List<String> order,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "100") Integer pageSize,
            @RequestParam(value = "expand", required = false) List<String> expand,
            @RequestParam(value = "projection", required = false) String projection
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
            @RequestParam(value = "expand", required = false) List<String> expand,
            @RequestParam(name = "projection", required = false) String projection
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
            @RequestParam(name = "projection", required = false) String projection
    ) throws CreateException {
        
        MANAGED_ENTITY_TYPE entity = (MANAGED_ENTITY_TYPE) this.createInputMapper.map(input);
        
        entity = (MANAGED_ENTITY_TYPE) this.service.create(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        return this.projectionService.project(normalizedProjection, entity);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update:}")
    public Object update(
            @RequestBody @Valid UPDATE_INPUT_TYPE input,
            @RequestParam(name = "projection", required = false) String projection
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
