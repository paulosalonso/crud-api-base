package com.alon.spring.crud.service;

import com.alon.querydecoder.Expression;
import com.alon.querydecoder.SingleExpression;
import com.alon.spring.crud.model.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface CrudService<R extends JpaRepository & JpaSpecificationExecutor> {
    
    public R getRepository();
    
    default <E extends BaseEntity> Page<E> list() {
        return this.list(0, Integer.MAX_VALUE);
    }
    
    default <E extends BaseEntity> Page<E> list(SingleExpression order) {
        return this.list(0, Integer.MAX_VALUE, order);
    }
    
    default <E extends BaseEntity> Page<E> list(int page, int size) {
        return this.getRepository().findAll(PageRequest.of(page, size));
    }

    default <E extends BaseEntity> Page<E> list(int page, int size, SingleExpression order) {
        return this.getRepository().findAll(Hidden.buildPageable(page, size, order));
    }
    
    default <E extends BaseEntity> Page<E> list(Specification<E> specification, int page, int size) {
        return this.getRepository().findAll(specification, PageRequest.of(page, size));
    }

    default <E extends BaseEntity> Page<E> list(Specification<E> specification, int page, int size, SingleExpression order) {
        return this.getRepository().findAll(specification, Hidden.buildPageable(page, size, order));
    }

    default <E extends BaseEntity> E create(@Valid E entity) throws CreateException {
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_CREATE);
            entity = (E) this.getRepository().save(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    default <ID, E extends BaseEntity<ID>> E read(ID id) {
        return (E) this.getRepository().findById(id).get();
    }

    default <E extends BaseEntity> E update(@Valid E entity) throws UpdateException {
        if (entity.id() == null)
            throw new UpdateException("Unmanaged entity. Use the create method.");
            
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_UPDATE);
            entity = this.create(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    default <ID> void delete(ID id) throws DeleteException {
        try {
            Hidden.executeHook(this, id, LifeCycleHook.BEFORE_DELETE);
            this.getRepository().deleteById(id);
            Hidden.executeHook(this, id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default <E extends BaseEntity> CrudService addBeforeCreateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_CREATE).add(function);
    	return this;
    }
    
    default <E extends BaseEntity> CrudService addAfterCreateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    default <E extends BaseEntity> CrudService addBeforeUpdateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    default <E extends BaseEntity> CrudService addAfterUpdateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    default <E extends BaseEntity> CrudService addBeforeDeleteHook(Function<Long, Long> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    default <E extends BaseEntity> CrudService addAfterDeleteHook(Function<Long, Long> function) {
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
     * This class has the function of restricting methods, 
     * because private methods are not allowed in interfaces.
     */
    class Hidden {
        
        private static Map<CrudService, Map<LifeCycleHook, List<Function>>> GLOBAL_HOOKS = new HashMap<>();
        
        private static Pageable buildPageable(int page, int size, SingleExpression orders) {
            return PageRequest.of(page, size, buildSort(orders));
        }

        private static Sort buildSort(SingleExpression order) {
            
            List<Order> orders = new ArrayList<>();

            do {
                boolean desc = order.getValue().equalsIgnoreCase("DESC");

                if (desc)
                    orders.add(Order.desc(order.getField()));
                else
                    orders.add(Order.asc(order.getField()));

                order = (SingleExpression) order.getNext();
            } while (order != null);

            return Sort.by(orders);
            
        }
    
        private static <S extends CrudService, P> P executeHook(S service, P param, LifeCycleHook hookType) throws Throwable {
            
            List<Function> hooks = getHook(service, hookType);

            for (Function hook : hooks)
                param = (P) hook.apply(param);

            return param;
            
        }

        private static <T extends CrudService> List<Function> getHook(T service, LifeCycleHook hookType) {
            return getServiceHooks(service).get(hookType);
        }
        
        private static <T extends CrudService> Map<LifeCycleHook, List<Function>> getServiceHooks(T service) {
            
            Map<LifeCycleHook, List<Function>> hooks = GLOBAL_HOOKS.get(service);

            if (hooks == null)
                hooks = initHooks(service);

            return hooks;
            
        }
        
        private static <T extends CrudService> Map<LifeCycleHook, List<Function>> initHooks(T service) {
            
            Map<LifeCycleHook, List<Function>> hooks = new HashMap<>();

            for (LifeCycleHook hook : LifeCycleHook.values())
                hooks.put(hook, new ArrayList<>());

            GLOBAL_HOOKS.put(service, hooks);

            return hooks;
            
        }
        
    }
}
