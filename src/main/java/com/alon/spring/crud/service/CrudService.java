package com.alon.spring.crud.service;

import com.alon.querydecoder.Expression;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alon.spring.crud.model.BaseEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

public interface CrudService<ID, E extends BaseEntity<ID>, R extends JpaRepository<E, ID> & JpaSpecificationExecutor<E>> {
	
    static Map<CrudService, Map<LifeCycleHook, List<CheckedFunction>>> GLOBAL_HOOKS = new HashMap<>();
    
    public R getRepository();

    public List<Sort.Order> getDefaultSort();
    
    default public Page<E> list(int page, int size) {
        return this.list(page, size, null);
    }
    
    default public Page<E> list(Specification<E> specification, int page, int size) {
        return this.list(specification, page, size, null);
    }

    default public Page<E> list(int page, int size, Expression order) {
        return this.getRepository().findAll(this.buildPageable(page, size, order));
    }

    default public Page<E> list(Specification<E> specification, int page, int size, Expression order) {
        return this.getRepository().findAll(specification, this.buildPageable(page, size, order));
    }

    default Pageable buildPageable(int page, int size, Expression orders) {
        return PageRequest.of(page, size, this.buildSort(orders));
    }

    default Sort buildSort(Expression order) {
        if (order.getField() == null)
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

    default public E create(@Valid E entity) throws CreateException {
        try {
            entity = this.executeHook(entity, LifeCycleHook.BEFORE_CREATE);
            entity = this.getRepository().save(entity);
            return this.executeHook(entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    default public E read(ID id) {
        return this.getRepository().findById(id).get();
    }

    default public E update(@Valid E entity) throws UpdateException {
        if (entity.getId() == null)
            throw new UpdateException("Unmanaged entity. Use the create method.");
            
        try {
            entity = this.executeHook(entity, LifeCycleHook.BEFORE_UPDATE);
            entity = this.create(entity);
            return this.executeHook(entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }


    default public void delete(ID id) throws DeleteException {
        try {
            this.executeHook(id, LifeCycleHook.BEFORE_DELETE);
            this.getRepository().deleteById(id);
            this.executeHook(id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default Map<LifeCycleHook, List<CheckedFunction>> getLocalHooks() {
        Map<LifeCycleHook, List<CheckedFunction>> hooks = GLOBAL_HOOKS.get(this);
        
        if (hooks == null) {
            hooks = new HashMap<>();
            
            for (LifeCycleHook hook : LifeCycleHook.values())
                hooks.put(hook, new ArrayList<>());
            
            GLOBAL_HOOKS.put(this, hooks);
        }
        
        return hooks;
    }
    
    default public CrudService addBeforeCreateHook(CheckedFunction<E, E> function) {
    	this.getLocalHooks().get(LifeCycleHook.BEFORE_CREATE).add(function);
    	return this;
    }
    
    default public CrudService addAfterCreateHook(CheckedFunction<E, E> function) {
    	this.getLocalHooks().get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    default public CrudService addBeforeUpdateHook(CheckedFunction<E, E> function) {
    	this.getLocalHooks().get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    default public CrudService addAfterUpdateHook(CheckedFunction<E, E> function) {
    	this.getLocalHooks().get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    default public CrudService addBeforeDeleteHook(CheckedFunction<Long, Long> function) {
    	this.getLocalHooks().get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    default public CrudService addAfterDeleteHook(CheckedFunction<Long, Long> function) {
    	this.getLocalHooks().get(LifeCycleHook.AFTER_DELETE).add(function);    	
    	return this;
    }
    
    default <P> P executeHook(P param, LifeCycleHook hookType) throws Throwable {
    	List<CheckedFunction> hooks = this.getHook(hookType);
        
    	for (CheckedFunction hook : hooks)
            param = (P) hook.apply(param);
        
        return param;
    }
    
    default List<CheckedFunction> getHook(LifeCycleHook hookType) {
    	return this.getLocalHooks().get(hookType);
    }
        
    enum LifeCycleHook {
    	BEFORE_CREATE,
    	AFTER_CREATE,
    	BEFORE_UPDATE,
    	AFTER_UPDATE,
    	BEFORE_DELETE,
    	AFTER_DELETE
    }
}
