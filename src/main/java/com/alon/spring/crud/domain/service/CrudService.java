package com.alon.spring.crud.domain.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.exception.CreateException;
import com.alon.spring.crud.domain.service.exception.DeleteException;
import com.alon.spring.crud.domain.service.exception.NotFoundException;
import com.alon.spring.crud.domain.service.exception.ReadException;
import com.alon.spring.crud.domain.service.exception.UpdateException;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;

public interface CrudService<
        ENTITY_ID_TYPE extends Serializable, 
        ENTITY_TYPE extends BaseEntity<ENTITY_ID_TYPE>, 
        REPOSITORY extends EntityGraphJpaRepository<ENTITY_TYPE, ENTITY_ID_TYPE> & EntityGraphJpaSpecificationExecutor<ENTITY_TYPE>
> {
    
    public REPOSITORY getRepository();

    default Page<ENTITY_TYPE> search(SearchCriteria criteria) {
    	
        try {
            HookHelper.executeHook(this, criteria, LifeCycleHook.BEFORE_SEARCH);
            
            Pageable pageable = PageHelper.buildPageable(criteria);
            
            Page<ENTITY_TYPE> searchResult;

            switch (criteria.getSearchOption()) {
                case FILTER:                    
                    searchResult = this.getRepository().findAll(criteria.getFilter(), pageable); 
                    break;
                    
                case EXPAND: 
                    searchResult = this.getRepository().findAll(pageable, criteria.getExpand()); 
                    break;
                        
                case FILTER_EXPAND:
            		searchResult = this.getRepository().findAll(criteria.getFilter(), pageable, criteria.getExpand()); 
                    break;
                    
                case NONE:
                default: searchResult = this.getRepository().findAll(pageable);
            }
            
            HookHelper.executeHook(this, searchResult, LifeCycleHook.AFTER_SEARCH);
            
            return searchResult;
        } catch (Throwable ex) {
            String message = String.format("Error searching entities: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }

    }

    @Transactional(rollbackOn = Throwable.class)
    default ENTITY_TYPE create(@Valid ENTITY_TYPE entity) {
        try {
            entity = HookHelper.executeHook(this, entity, LifeCycleHook.BEFORE_CREATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return HookHelper.executeHook(this, entity, LifeCycleHook.AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }
    
    default ENTITY_TYPE read(ENTITY_ID_TYPE id) {
        return this.read(id, null);
    }

    default ENTITY_TYPE read(ENTITY_ID_TYPE id, List<String> expand) {
        try {
            id = HookHelper.executeHook(this, id, LifeCycleHook.BEFORE_READ);
            
            Optional<ENTITY_TYPE> opt;
            
            if (expand != null && !expand.isEmpty())
                opt = this.getRepository().findById(id, new DynamicEntityGraph(expand));
            else
                opt = this.getRepository().findById(id);

            if (opt.isEmpty())
                throw new NotFoundException(String.format("ID not found -> %d", id));

            id = HookHelper.executeHook(this, id, LifeCycleHook.AFTER_READ);
            
            return opt.get();
        } catch (Throwable ex) {
            String message = String.format("Error reading entity: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }
    }

    @Transactional(rollbackOn = Throwable.class)
    default ENTITY_TYPE update(@Valid ENTITY_TYPE entity) {
        try {
            if(!this.getRepository().existsById(entity.getId()))
                throw new UpdateException("Entity to update not found");

            entity = HookHelper.executeHook(this, entity, LifeCycleHook.BEFORE_UPDATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return HookHelper.executeHook(this, entity, LifeCycleHook.AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    @Transactional(rollbackOn = Throwable.class)
    default void delete(ENTITY_ID_TYPE id) {
        try {
        	HookHelper.executeHook(this, id, LifeCycleHook.BEFORE_DELETE);
            this.getRepository().deleteById(id);
            HookHelper.executeHook(this, id, LifeCycleHook.AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default void addBeforeSearchHook(Function<SearchCriteria, SearchCriteria> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.BEFORE_SEARCH).add(function);
    }
    
    default void addAfterSearchHook(Function<Page<ENTITY_TYPE>, Page<ENTITY_TYPE>> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.AFTER_SEARCH).add(function);
    }
    
    default void addBeforeReadHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.BEFORE_READ).add(function);
    }
    
    default void addAfterReadHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.AFTER_READ).add(function);
    }
    
    default void addBeforeCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.BEFORE_CREATE).add(function);
    }
    
    default void addAfterCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.AFTER_CREATE).add(function);
    }
    
    default void addBeforeUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.BEFORE_UPDATE).add(function);
    }
    
    default void addAfterUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.AFTER_UPDATE).add(function);
    }
    
    default void addBeforeDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.BEFORE_DELETE).add(function);
    }
    
    default void addAfterDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(LifeCycleHook.AFTER_DELETE).add(function);
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
    
    static class PageHelper {

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
        
    }
    
    static class HookHelper {
    	
    	private static Map<Class<? extends CrudService>, Map<LifeCycleHook, List<Function>>> GLOBAL_HOOKS = new HashMap<>();
    	
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
            
            Map<LifeCycleHook, List<Function>> hooks = GLOBAL_HOOKS.get(service.getClass());

            if (hooks == null)
                hooks = initHooks(service);

            return hooks;
            
        }
        
        private static <T extends CrudService> Map<LifeCycleHook, List<Function>> initHooks(T service) {
            
            Map<LifeCycleHook, List<Function>> hooks = new HashMap<>();

            for (LifeCycleHook hook : LifeCycleHook.values())
                hooks.put(hook, new ArrayList<>());

            GLOBAL_HOOKS.put(service.getClass(), hooks);

            return hooks;
            
        }
    	
    }
    
}
