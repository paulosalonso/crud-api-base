package com.alon.spring.crud.resource;

import com.alon.querydecoder.SingleExpressionParser;
import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.repository.specification.SpringJpaSpecification;
import com.alon.spring.crud.resource.dto.*;
import com.alon.spring.crud.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

public abstract class CrudResource<E extends BaseEntity, C, U, S extends CrudService> {
	
    @Autowired
    protected S service;

    private OutputDtoConverter<Page<E>, ListOutput<Object>> listOutputConverter = new EntityListOutputConverter();
    private OutputDtoConverter<E, Object> outputConverter = new EntityOutputConverter();
    private InputDtoConverter<C, E> createInputConverter = new EntityInputConverter();
    private InputDtoConverter<U, E> updateInputConverter = new EntityInputConverter();

    public void setOutputConverter(OutputDtoConverter<E, Object> outputConverter) {
        this.outputConverter = outputConverter;

        listOutputConverter = new OutputDtoConverter<>() {
            @Override
            public ListOutput<Object> convert(Page<E> data) {
                return ListOutput.of(data, outputConverter);
            }
        };
    }

    public void setCreateInputConverter(InputDtoConverter<C, E> createInputConverter) {
        this.createInputConverter = createInputConverter;
    }

    public void setUpdateInputConverter(InputDtoConverter<U, E> updateInputConverter) {
        this.updateInputConverter = updateInputConverter;
    }

    @GetMapping("${com.alon.spring.crud.path.list}")
    public ListOutput<Object> list(
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
        
        return this.listOutputConverter
                   .convert(entities);
        
    }

    @GetMapping("${com.alon.spring.crud.path.read}")
    public Object read(@PathVariable Long id) throws NotFoundException {
        
        E entity = (E) this.service.read(id);
        
        return this.outputConverter
                   .convert(entity);
        
    }

    @PostMapping("${com.alon.spring.crud.path.create}")
    @ResponseStatus(HttpStatus.CREATED)
    protected Object create(@RequestBody C input) throws CreateException {
        
        E entity = (E) this.createInputConverter
                           .convert(input);
        
        entity = (E) this.service.create(entity);
        
        return this.outputConverter
                   .convert(entity);
        
    }

    @PutMapping("${com.alon.spring.crud.path.update}")
    public Object update(@RequestBody U input) throws UpdateException {
        
        E entity = (E) this.updateInputConverter
                           .convert(input);

        entity = (E) this.service.update(entity);
        
        return this.outputConverter
                   .convert(entity);
        
    }

    @DeleteMapping("${com.alon.spring.crud.path.delete}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws DeleteException {
        this.service.delete(id);
    }
}
