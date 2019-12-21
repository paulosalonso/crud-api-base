package com.alon.spring.crud.service;

import com.alon.querydecoder.SingleExpression;
import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.service.exception.CreateException;
import com.alon.spring.crud.service.exception.DeleteException;
import com.alon.spring.crud.service.exception.NotFoundException;
import com.alon.spring.crud.service.exception.ReadException;
import com.alon.spring.crud.service.exception.UpdateException;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public interface CrudService<I extends Serializable, E extends BaseEntity<I>, R extends EntityGraphJpaRepository<E, I> & EntityGraphJpaSpecificationExecutor<E>> {
    
    public R getRepository();

    default Page<E> search(SearchCriteria criteria) {
        
        try {
            Hidden.executeHook(this, criteria, LifeCycleHook.BEFORE_SEARCH);
            Pageable pageable = Hidden.buildPageable(criteria);
            Page<E> result;

            switch (criteria.getSearchOption()) {
                case SPECIFICATION:
                case SPECIFICATION_ORDER: 
                    
                    result = this.getRepository()
                        .findAll(criteria.getFilter(), pageable); 
                    break;
                        
                case SPECIFICATION_EXPAND:
                case SPECIFICATION_ORDER_EXPAND: 
                    
                    result = this.getRepository()
                            .findAll(criteria.getFilter(), pageable, criteria.getExpand()); 
                    break;
                        
                case ORDER_EXPAND:
                case EXPAND: 
                    
                    result = this.getRepository()
                            .findAll(pageable, criteria.getExpand()); 
                    break;
                    
                case ORDER:
                case NONE:
                default: result = this.getRepository().findAll(pageable);
            }
            
            Hidden.executeHook(this, result, LifeCycleHook.AFTER_SEARCH);
            
            return result;
        } catch (Throwable ex) {
            String message = String.format("Error searching entities: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }

    }

    default E create(@Valid E entity) {
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_CREATE);
            entity = (E) this.getRepository().save(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    default E read(I id) {
        try {
            id = Hidden.executeHook(this, id, LifeCycleHook.BEFORE_READ);
            Optional<E> opt = this.getRepository().findById(id);

            if (opt.isEmpty())
                throw new NotFoundException(String.format("ID not found -> %d", id));

            id = Hidden.executeHook(this, id, LifeCycleHook.AFTER_READ);
            
            return opt.get();
        } catch (Throwable ex) {
            String message = String.format("Error reading entity: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }
    }

    default E update(@Valid E entity) {
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

    default void delete(I id) {
        try {
            Hidden.executeHook(this, id, LifeCycleHook.BEFORE_DELETE);
            this.getRepository().deleteById(id);
            Hidden.executeHook(this, id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default CrudService addBeforeSearchHook(Function<SearchCriteria, SearchCriteria> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_SEARCH).add(function);
    	return this;
    }
    
    default CrudService addAfterSearchHook(Function<Page<E>, Page<E>> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_SEARCH).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeReadHook(Function<I, I> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_READ).add(function);
    	return this;
    }
    
    default CrudService addAfterReadHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_READ).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeCreateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_CREATE).add(function);
    	return this;
    }
    
    default CrudService addAfterCreateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeUpdateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterUpdateHook(Function<E, E> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeDeleteHook(Function<Long, Long> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterDeleteHook(Function<Long, Long> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_DELETE).add(function);    	
    	return this;
    }
        
    enum LifeCycleHook {
    	BEFORE_SEARCH,
        AFTER_SEARCH,
        BEFORE_READ,
        AFTER_READ,
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

        private static Pageable buildPageable(SearchCriteria criteria) {
            if (criteria.getOrder() != null)
                return PageRequest.of(criteria.getPage(), criteria.getSize(), buildSort(criteria.getOrder()));
            else
                return PageRequest.of(criteria.getPage(), criteria.getSize());
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
