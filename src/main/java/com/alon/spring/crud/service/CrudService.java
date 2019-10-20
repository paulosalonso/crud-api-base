package com.alon.spring.crud.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alon.querydecoder.Expression;
import com.alon.spring.crud.model.BaseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public abstract class CrudService<E extends BaseEntity, R extends JpaRepository<E, Long> & JpaSpecificationExecutor<E>> {
	
    @Autowired
    protected R repository;
    
    private final Map<LifeCycleHook, List<CheckedFunction>> HOOKS = new HashMap<>(); 

    public CrudService() {
        
        for (LifeCycleHook hook : LifeCycleHook.values())
            this.HOOKS.put(hook, new ArrayList<>());
        
        this.addBeforeUpdateHook(this::checkEntityState);
        this.addBeforeUpdateHook(this::entityExists);
    }

    public Page<E> list(int page, int size) {
        return this.list(page, size, null);
    }

    public Page<E> list(int page, int size, Expression order) {
        return this.repository.findAll(this.buildPageable(page, size, order));
    }

    public Page<E> list(Specification<E> specification, int page, int size, Expression order) {
        return this.repository.findAll(specification, this.buildPageable(page, size, order));
    }

    private Pageable buildPageable(int page, int size, Expression orders) {
        return PageRequest.of(page, size, this.getSort(orders));
    }

    private Sort getSort(Expression order) {
        if (order == null || order.getField() == null)
            return Sort.by(this.getDefaultSort());

        List<Order> orders = new ArrayList<>();

        do {
            boolean desc = order.getValue().equalsIgnoreCase("DESC");

            if (desc)
                orders.add(Order.desc(order.getField()));
            else
                orders.add(Order.asc(order.getField()));

            order = (Expression) order.getNext();
        } while (order != null);

        return Sort.by(orders);
    }

    public E create(@Valid E entity) throws CreateException {
        try {
            entity = this.executeHook(entity, LifeCycleHook.BEFORE_CREATE);
            entity = this.repository.save(entity);
            return this.executeHook(entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    public E read(Long id) {
        return this.repository.findById(id).get();
    }

    public E update(@Valid E entity) throws UpdateException {            
        try {
            entity = this.executeHook(entity, LifeCycleHook.BEFORE_UPDATE);
            entity = this.create(entity);
            return this.executeHook(entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    public void delete(Long id) throws DeleteException {
        try {
            this.executeHook(id, LifeCycleHook.BEFORE_DELETE);
            this.repository.deleteById(id);
            this.executeHook(id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    private E checkEntityState(E entity) throws UpdateException {        
        if (entity.getId() == null)
            throw new UpdateException("Unmanaged entity. Use the create method.");
        
        return entity;
    }
    
    private E entityExists(E entity) throws UpdateException {
        boolean exists = this.repository.existsById(entity.getId());
        
        if (!exists)
            throw new UpdateException("Entity not exists");
        
        return entity;
    }

    public abstract List<Order> getDefaultSort();
    
    public final CrudService addBeforeCreateHook(CheckedFunction<E, E> function) {
    	this.HOOKS.get(LifeCycleHook.BEFORE_CREATE).add(function);    	
    	return this;
    }
    
    public final CrudService addAfterCreateHook(CheckedFunction<E, E> function) {
    	this.HOOKS.get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    public final CrudService addBeforeUpdateHook(CheckedFunction<E, E> function) {
    	this.HOOKS.get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    public final CrudService addAfterUpdateHook(CheckedFunction<E, E> function) {
    	this.HOOKS.get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    public final CrudService addBeforeDeleteHook(CheckedFunction<Long, Long> function) {
    	this.HOOKS.get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    public final CrudService addAfterDeleteHook(CheckedFunction<Long, Long> function) {
    	this.HOOKS.get(LifeCycleHook.AFTER_DELETE).add(function);    	
    	return this;
    }
    
    private <O> O executeHook(O param, LifeCycleHook hookType) throws Throwable {
    	List<CheckedFunction> hooks = this.getHook(hookType);
        
    	for (CheckedFunction hook : hooks)
            param = (O) hook.apply(param);
        
        return param;
    }
    
    private List<CheckedFunction> getHook(LifeCycleHook hookType) {
    	return HOOKS.get(hookType);
    }
        
    private enum LifeCycleHook {
    	BEFORE_CREATE,
    	AFTER_CREATE,
    	BEFORE_UPDATE,
    	AFTER_UPDATE,
    	BEFORE_DELETE,
    	AFTER_DELETE
    }
}
