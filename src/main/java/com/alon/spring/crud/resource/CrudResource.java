package com.alon.spring.crud.resource;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.alon.spring.crud.service.CrudService;
import com.alon.querydecoder.Expression;
import com.alon.querydecoder.impl.SpringJpaSpecificationDecoder;
import com.alon.spring.crud.model.BaseEntity;

public abstract class CrudResource<E extends BaseEntity, S extends CrudService<E, ?>> {
	
	protected S service;

	public CrudResource(S service) {
		this.service = service;
	}
	
	@GetMapping
	public Page<E> list(
                @RequestParam(value = "filter", required = false)                              String  filter,
                @RequestParam(value = "order",  required = false, defaultValue = "")           String  order,
                @RequestParam(value = "page",   required = false, defaultValue = "0")          Integer page, 
                @RequestParam(value = "size",   required = false, defaultValue = "0X7fffffff") Integer size
	) {
		if (filter == null)
                    return this.service.list(page, size, new Expression(order));
		else
                    return this.service.list(new SpringJpaSpecificationDecoder<>(filter), page, size, new Expression(order));
	}
	
	@GetMapping("/{id}")
	public E read(@PathVariable Long id) {
            return this.service.read(id);
	}
	
	@PostMapping
	protected E create(@RequestBody E entity) throws Exception {
            return this.service.create(entity);
	}
	
	@PutMapping("/{id}")
	public E update(@RequestBody E entity) throws Exception {
            return this.service.update(entity);
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) throws Exception {
            this.service.delete(id);
	}
}
