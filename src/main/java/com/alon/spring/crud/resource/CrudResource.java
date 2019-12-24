package com.alon.spring.crud.resource;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.resource.input.EntityInputMapper;
import com.alon.spring.crud.resource.input.InputMapper;
import com.alon.spring.crud.resource.projection.OutputPage;
import com.alon.spring.crud.service.CrudService;
import com.alon.spring.crud.service.ProjectionService;
import com.alon.spring.crud.service.SearchCriteria;
import com.alon.spring.crud.service.exception.CreateException;
import com.alon.spring.crud.service.exception.DeleteException;
import com.alon.spring.crud.service.exception.ReadException;
import com.alon.spring.crud.service.exception.UpdateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

public abstract class CrudResource<E extends BaseEntity, C, U, S extends CrudService> {
	
    @Autowired
    protected S service;

    @Autowired
    protected ProjectionService projectionService;

    private InputMapper<C, E> createInputMapper = new EntityInputMapper();
    private InputMapper<U, E> updateInputMapper = new EntityInputMapper();

    public void setCreateInputMapper(InputMapper<C, E> createInputMapper) {
        this.createInputMapper = createInputMapper;
    }

    public void setUpdateInputMapper(InputMapper<U, E> updateInputMapper) {
        this.updateInputMapper = updateInputMapper;
    }

    @GetMapping("${com.alon.spring.crud.path.search:}")
    public OutputPage search(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size,
            @RequestParam(value = "expand", required = false, defaultValue = "") List<String> expand,
            @RequestParam(value = "projection", required = false) Optional<String> projection
    ) {
        
        String normalizedProjection = this.normalizeProjection(projection);
        List<String> normalizedExpand = this.normalizeExpand(normalizedProjection, expand);

        page = normalizePage(page);

        SearchCriteria criteria = SearchCriteria.of()
                .filter(filter)
                .order(order)
                .page(page)
                .size(size)
                .expand(expand)
                .build();

        Page<E> entities = this.service.search(criteria);
        
        return this.projectionService.project(normalizedProjection, entities, normalizedExpand);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read:/{id}}")
    public Object read(
            @PathVariable Long id,
            @RequestParam(value = "expand", required = false, defaultValue = "") List<String> expand,
            @RequestParam(name = "projection", required = false) Optional<String> projection
    ) throws ReadException {
        
        String normalizedProjection = this.normalizeProjection(projection);
        List<String> normalizedExpand = this.normalizeExpand(normalizedProjection, expand);
        
        E entity = (E) this.service.read(id, expand);
        
        return this.projectionService.project(normalizedProjection, entity, normalizedExpand);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create:}")
    @ResponseStatus(HttpStatus.CREATED)
    protected Object create(
            @RequestBody @Valid C input,
            @RequestParam(name = "projection", required = false) Optional<String> projection
    ) throws CreateException {
        
        E entity = (E) this.createInputMapper.convert(input);
        
        entity = (E) this.service.create(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        return this.projectionService.project(normalizedProjection, entity, List.of());
        
    }

    @PutMapping("${com.alon.spring.crud.path.update:}")
    public Object update(
            @RequestBody @Valid U input,
            @RequestParam(name = "projection", required = false) Optional<String> projection
    ) throws UpdateException {
        
        E entity = (E) this.updateInputMapper.convert(input);

        entity = (E) this.service.update(entity);
        
        String normalizedProjection = this.normalizeProjection(projection);
        
        return this.projectionService.project(normalizedProjection, entity, List.of());
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete:/{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws DeleteException {
        this.service.delete(id);
    }
    
    /**
     * Override this method to change the default projection
     */
    protected String defaultProjection() {
        return ProjectionService.ENTITY_PROJECTION;
    }
    
    private int normalizePage(int page) {
        return --page;
    }
    
    private String normalizeProjection(Optional<String> projection) {
        return projection.orElse(this.defaultProjection());
    }
    
    private List<String> normalizeExpand(String projectionName, List<String> receivedExpand) {
        
        if (projectionName.equals(ProjectionService.ENTITY_PROJECTION))
            return receivedExpand;
        
        return this.projectionService.getRequiredExpand(projectionName);
        
    }

}
