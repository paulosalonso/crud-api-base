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
    
    public R getRepository();

    public List<Sort.Order> getDefaultSort();
    
    default Page<E> list() {
        return this.list(0, Integer.MAX_VALUE);
    }
    
    default Page<E> list(Expression order) {
        return this.list(0, Integer.MAX_VALUE, order);
    }
    
    default Page<E> list(int page, int size) {
        return this.list(page, size, null);
    }
    
    default Page<E> list(Specification<E> specification, int page, int size) {
        return this.list(specification, page, size, null);
    }

    default Page<E> list(int page, int size, Expression order) {
        return this.getRepository().findAll(Hidden.buildPageable(page, size, order, this.getDefaultSort()));
    }

    default Page<E> list(Specification<E> specification, int page, int size, Expression order) {
        return this.getRepository().findAll(specification, Hidden.buildPageable(page, size, order, this.getDefaultSort()));
    }

    default E create(@Valid E entity) throws CreateException {
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_CREATE);
            entity = this.getRepository().save(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    default E read(ID id) {
        return this.getRepository().findById(id).get();
    }

    default E update(@Valid E entity) throws UpdateException {
        if (entity.getId() == null)
            throw new UpdateException("Unmanaged entity. Use the create method.");
            
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_UPDATE);
            entity = this.create(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    default void delete(ID id) throws DeleteException {
        try {
            Hidden.executeHook(this, id, LifeCycleHook.BEFORE_DELETE);
            this.getRepository().deleteById(id);
            Hidden.executeHook(this, id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default CrudService addBeforeCreateHook(CheckedFunction<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_CREATE).add(function);
    	return this;
    }
    
    default CrudService addAfterCreateHook(CheckedFunction<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeUpdateHook(CheckedFunction<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterUpdateHook(CheckedFunction<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeDeleteHook(CheckedFunction<Long, Long> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterDeleteHook(CheckedFunction<Long, Long> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_DELETE).add(function);    	
    	return this;
    }
        
    enum LifeCycleHook {
    	BEFORE_CREATE,
    	AFTER_CREATE,
    	BEFORE_UPDATE,
    	AFTER_UPDATE,
    	BEFORE_DELETE,
    	AFTER_DELETE
    }
    
    /**
     * This class has the function of simulating private methods, which is not 
     * allowed in interfaces
     */
    class Hidden {
        
        private static Map<CrudService, Map<LifeCycleHook, List<CheckedFunction>>> GLOBAL_HOOKS = new HashMap<>();
        
        private static Pageable buildPageable(int page, int size, Expression orders, List<Order> defaultSort) {
            return PageRequest.of(page, size, buildSort(orders, defaultSort));
        }

        private static Sort buildSort(Expression order, List<Order> defaultSort) {
            
            if (order == null || order.getField() == null)
                return Sort.by(defaultSort);

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
    
        private static <S extends CrudService, P> P executeHook(S service, P param, LifeCycleHook hookType) throws Throwable {
            
            List<CheckedFunction> hooks = getHook(service, hookType);

            for (CheckedFunction hook : hooks)
                param = (P) hook.apply(param);

            return param;
            
        }

        private static <T extends CrudService> List<CheckedFunction> getHook(T service, LifeCycleHook hookType) {
            return getServiceHooks(service).get(hookType);
        }
        
        private static <T extends CrudService> Map<LifeCycleHook, List<CheckedFunction>> getServiceHooks(T service) {
            
            Map<LifeCycleHook, List<CheckedFunction>> hooks = GLOBAL_HOOKS.get(service);

            if (hooks == null)
                hooks = initHooks(service);

            return hooks;
            
        }
        
        private static <T extends CrudService> Map<LifeCycleHook, List<CheckedFunction>> initHooks(T service) {
            
            Map<LifeCycleHook, List<CheckedFunction>> hooks = new HashMap<>();

            for (LifeCycleHook hook : LifeCycleHook.values())
                hooks.put(hook, new ArrayList<>());

            GLOBAL_HOOKS.put(service, hooks);

            return hooks;
            
        }
        
    }
}
