package com.alon.spring.crud.resource;

import com.alon.querydecoder.ExpressionParser;
import com.alon.querydecoder.SingleExpressionParser;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.alon.querydecoder.Expression;
import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.repository.specification.SpringJpaSpecification;
import com.alon.spring.crud.resource.dto.EntityConverterProvider;
import com.alon.spring.crud.resource.dto.ResourceDtoConverterProvider;
import com.alon.spring.crud.service.CreateException;
import com.alon.spring.crud.service.CrudService;
import com.alon.spring.crud.service.DeleteException;
import com.alon.spring.crud.service.NotFoundException;
import com.alon.spring.crud.service.UpdateException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 
 * @param <S> Service
 */
public abstract class CrudResource<C, U, S extends CrudService> {
	
    @Autowired
    protected S service;
    
    @Autowired
    private EntityConverterProvider entityConverterProvider;
    
    public ResourceDtoConverterProvider getDtoConverterProvider() {
        return this.entityConverterProvider;
    }

    @GetMapping("${com.alon.spring.crud.path.list}")
    public <E extends BaseEntity, O> O list(
            @RequestParam(value = "filter", required = false)                       Optional<String> filter,
            @RequestParam(value = "order",  required = false)                       Optional<String> order,
            @RequestParam(value = "page",   required = false, defaultValue = "0")   Integer page,
            @RequestParam(value = "size",   required = false, defaultValue = "100") Integer size
    ) {
        
        Page<E> entities;
        
        if (filter.isPresent() && order.isPresent())
            entities = this.service.list(SpringJpaSpecification.of(filter.get()), 0, 0, SingleExpressionParser.parse(order.get()));
        else if (filter.isPresent())
            entities = this.service.list(SpringJpaSpecification.of(filter.get()), page, size);
        else if (order.isPresent())
            entities = this.service.list(page, size, SingleExpressionParser.parse(order.get()));
        else
            entities = this.service.list(page, size);
        
        return (O) this.getDtoConverterProvider()
                       .getListOutputDtoConverter()
                       .convert(entities);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read}")
    public <E extends BaseEntity, O> O read(@PathVariable Long id) throws NotFoundException {
        
        E entity = (E) this.service.read(id);
        
        return (O) this.getDtoConverterProvider()
                       .getReadOutputDtoConverter()
                       .convert(entity);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create}")
    @ResponseStatus(HttpStatus.CREATED)
    protected <E extends BaseEntity, O> O create(@RequestBody C input) throws CreateException {
        
        E entity = (E) this.getDtoConverterProvider()
                           .getCreateInputDtoConverter()
                           .convert(input);
        
        entity = (E) this.service.create(entity);
        
        return (O) this.getDtoConverterProvider()
                       .getCreateOutputDtoConverter()
                       .convert(entity);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update}")
    public <E extends BaseEntity, O> O update(@RequestBody U input) throws UpdateException {
        
        E entity = (E) this.getDtoConverterProvider()
                           .getUpdateInputDtoConverter()
                           .convert(input);

        entity = (E) this.service.update(entity);
        
        return (O) this.getDtoConverterProvider()
                       .getUpdateOutputDtoConverter()
                       .convert(entity);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws DeleteException {
        this.service.delete(id);
    }
}
