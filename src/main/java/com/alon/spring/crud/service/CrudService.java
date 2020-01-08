package com.alon.spring.crud.service;

import com.alon.spring.crud.model.BaseEntity;
import com.alon.spring.crud.service.exception.CreateException;
import com.alon.spring.crud.service.exception.DeleteException;
import com.alon.spring.crud.service.exception.NotFoundException;
import com.alon.spring.crud.service.exception.ReadException;
import com.alon.spring.crud.service.exception.UpdateException;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
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

public interface CrudService<
        ENTITY_ID_TYPE extends Serializable, 
        ENTITY_TYPE extends BaseEntity<ENTITY_ID_TYPE>, 
        REPOSITORY extends EntityGraphJpaRepository<ENTITY_TYPE, ENTITY_ID_TYPE> & EntityGraphJpaSpecificationExecutor<ENTITY_TYPE>
> {
    
    public REPOSITORY getRepository();

    default Page<ENTITY_TYPE> search(SearchCriteria criteria) {
        
        try {
            Hidden.executeHook(this, criteria, LifeCycleHook.BEFORE_SEARCH);
            Pageable pageable = Hidden.buildPageable(criteria);
            Page<ENTITY_TYPE> result;

            switch (criteria.getSearchOption()) {
                case FILTER:
                case FILTER_ORDER: 
                    
                    result = this.getRepository()
                        .findAll(criteria.getFilter(), pageable); 
                    break;
                        
                case FILTER_EXPAND:
                case FILTER_ORDER_EXPAND: 
                    
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

    default ENTITY_TYPE create(@Valid ENTITY_TYPE entity) {
        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_CREATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }
    
    default ENTITY_TYPE read(ENTITY_ID_TYPE id) {
        return this.read(id, null);
    }

    default ENTITY_TYPE read(ENTITY_ID_TYPE id, List<String> expand) {
        try {
            id = Hidden.executeHook(this, id, LifeCycleHook.BEFORE_READ);
            
            Optional<ENTITY_TYPE> opt;
            
            if (expand != null && !expand.isEmpty())
                opt = this.getRepository().findById(id, new DynamicEntityGraph(expand));
            else
                opt = this.getRepository().findById(id);

            if (opt.isEmpty())
                throw new NotFoundException(String.format("ID not found -> %d", id));

            id = Hidden.executeHook(this, id, LifeCycleHook.AFTER_READ);
            
            return opt.get();
        } catch (Throwable ex) {
            String message = String.format("Error reading entity: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }
    }

    default ENTITY_TYPE update(@Valid ENTITY_TYPE entity) {
        if (entity.id() == null)
            throw new UpdateException("Unmanaged entity. Use the create method.");

        try {
            entity = Hidden.executeHook(this, entity, LifeCycleHook.BEFORE_UPDATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return Hidden.executeHook(this, entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    default void delete(ENTITY_ID_TYPE id) {
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
    
    default CrudService addAfterSearchHook(Function<Page<ENTITY_TYPE>, Page<ENTITY_TYPE>> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_SEARCH).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeReadHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_READ).add(function);
    	return this;
    }
    
    default CrudService addAfterReadHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_READ).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_CREATE).add(function);
    	return this;
    }
    
    default CrudService addAfterCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_CREATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.AFTER_UPDATE).add(function);    	
    	return this;
    }
    
    default CrudService addBeforeDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	Hidden.getServiceHooks(this).get(LifeCycleHook.BEFORE_DELETE).add(function);    	
    	return this;
    }
    
    default CrudService addAfterDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
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
                return PageRequest.of(criteria.getPage(), criteria.getPageSize(), buildSort(criteria.getOrder()));
            else
                return PageRequest.of(criteria.getPage(), criteria.getPageSize());
        }

        private static Sort buildSort(List<String> order) {
            
            List<Order> orders = new ArrayList<>();
            
            order.forEach(item -> {
                if (item.startsWith("-"))
                    orders.add(Order.desc(item.substring(1)));
                else
                    orders.add(Order.asc(item));
            });

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
