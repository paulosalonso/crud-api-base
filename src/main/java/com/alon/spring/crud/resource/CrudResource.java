package com.alon.spring.crud.resource;

import com.alon.querydecoder.SingleExpressionParser;
import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.repository.specification.SpringJpaSpecification;
import com.alon.spring.crud.resource.dto.EntityInputMapper;
import com.alon.spring.crud.resource.dto.InputMapper;
import com.alon.spring.crud.resource.dto.ListOutput;
import com.alon.spring.crud.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

public abstract class CrudResource<E extends BaseEntity, C, U, S extends CrudService> {
	
    @Autowired
    protected S service;

    @Autowired
    private ProjectionService projectionService;

    private InputMapper<C, E> createInputMapper = new EntityInputMapper();
    private InputMapper<U, E> updateInputMapper = new EntityInputMapper();

    public void setCreateInputMapper(InputMapper<C, E> createInputMapper) {
        this.createInputMapper = createInputMapper;
    }

    public void setUpdateInputMapper(InputMapper<U, E> updateInputMapper) {
        this.updateInputMapper = updateInputMapper;
    }

    @GetMapping("${com.alon.spring.crud.path.list:}")
    public ListOutput<Object> list(
            @RequestParam(value = "filter", required = false) Optional<String> filter,
            @RequestParam(value = "order", required = false) Optional<String> order,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "100") Integer size,
            @RequestParam(value = "projection", required = false, defaultValue = "default") String projection
    ) {

        page = normalizePage(page);

        Page<E> entities;

        if (filter.isPresent() && order.isPresent())
            entities = this.service.list(SpringJpaSpecification.of(filter.get()), 0, 0, SingleExpressionParser.parse(order.get()));
        else if (filter.isPresent())
            entities = this.service.list(SpringJpaSpecification.of(filter.get()), page, size);
        else if (order.isPresent())
            entities = this.service.list(page, size, SingleExpressionParser.parse(order.get()));
        else
            entities = this.service.list(page, size);
        
        return this.projectionService
                   .project(projection, entities);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read:/{id}}")
    public Object read(
            @PathVariable Long id,
            @RequestParam(name = "projection", required = false, defaultValue = "default") String projection
    ) throws NotFoundException {
        
        E entity = (E) this.service.read(id);
        
        return this.projectionService
                   .project(projection, entity);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create:}")
    @ResponseStatus(HttpStatus.CREATED)
    protected Object create(
            @RequestBody C input,
            @RequestParam(name = "projection", required = false, defaultValue = "default") String projection
    ) throws CreateException {
        
        E entity = (E) this.createInputMapper
                           .convert(input);
        
        entity = (E) this.service.create(entity);
        
        return this.projectionService
                   .project(projection, entity);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update:}")
    public Object update(
            @RequestBody U input,
            @RequestParam(name = "projection", required = false, defaultValue = "default") String projection
    ) throws UpdateException {
        
        E entity = (E) this.updateInputMapper
                           .convert(input);

        entity = (E) this.service.update(entity);
        
        return this.projectionService
                   .project(projection, entity);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete:/{id}}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws DeleteException {
        this.service.delete(id);
    }

    private int normalizePage(int page) {
        return --page;
    }

}
