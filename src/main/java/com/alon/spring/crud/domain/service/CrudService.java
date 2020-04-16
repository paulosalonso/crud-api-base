package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.service.exception.*;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static com.alon.spring.crud.domain.service.CrudService.HookHelper.LifeCycleHook.*;

public interface CrudService<
        ENTITY_ID_TYPE extends Serializable, 
        ENTITY_TYPE extends BaseEntity<ENTITY_ID_TYPE>, 
        REPOSITORY extends EntityGraphJpaRepository<ENTITY_TYPE, ENTITY_ID_TYPE> &
                EntityGraphJpaSpecificationExecutor<ENTITY_TYPE>
> {
    
    REPOSITORY getRepository();

    @Transactional(propagation = Propagation.SUPPORTS)
    default Page<ENTITY_TYPE> search(SearchCriteria criteria) {
    	
        try {
            HookHelper.executeHook(this, criteria, BEFORE_SEARCH);

            Pageable pageable = criteria.getPageable();
            
            Page<ENTITY_TYPE> searchResult;

            switch (criteria.getSearchOption()) {
                case FILTER:
                    searchResult = getRepository().findAll(criteria.getFilter(), pageable);
                    break;

                case EXPAND:
                    searchResult = getRepository().findAll(pageable, criteria.getExpand());
                    break;

                case FILTER_EXPAND:
            		searchResult = getRepository().findAll(criteria.getFilter(), pageable, criteria.getExpand());
                    break;

                case NONE:
                default: searchResult = getRepository().findAll(pageable);
            }

            HookHelper.executeHook(this, searchResult, AFTER_SEARCH);

            return searchResult;
        } catch (Throwable ex) {
            String message = String.format("Error searching entities: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }

    }

    @Transactional(rollbackFor = Throwable.class)
    default ENTITY_TYPE create(@Valid ENTITY_TYPE entity) {
        try {
            entity = HookHelper.executeHook(this, entity, BEFORE_CREATE);
            entity = (ENTITY_TYPE) getRepository().save(entity);
            return HookHelper.executeHook(this, entity, AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }
    
    default ENTITY_TYPE read(ENTITY_ID_TYPE id) {
        return this.read(id, null);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    default ENTITY_TYPE read(ENTITY_ID_TYPE id, List<String> expand) {
        try {
            id = HookHelper.executeHook(this, id, BEFORE_READ);

            Optional<ENTITY_TYPE> opt;

            if (expand != null && !expand.isEmpty())
                opt = getRepository().findById(id, new DynamicEntityGraph(expand));
            else
                opt = getRepository().findById(id);

            if (opt.isEmpty())
                throw new NotFoundException(String.format("ID not found -> %d", id));

            ENTITY_TYPE entity = opt.get();

            entity = HookHelper.executeHook(this, entity, AFTER_READ);

            return entity;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Throwable ex) {
            String message = String.format("Error reading entity: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    default ENTITY_TYPE update(@Valid ENTITY_TYPE entity) {
        if (!getRepository().existsById(entity.getId()))
            throw new NotFoundException("Entity to update not found");

        try {
            entity = HookHelper.executeHook(this, entity, BEFORE_UPDATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return HookHelper.executeHook(this, entity, AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    default void delete(ENTITY_ID_TYPE id) {
        if (!getRepository().existsById(id))
            throw new NotFoundException("Entity to delete not found");

        try {
        	HookHelper.executeHook(this, id, BEFORE_DELETE);
            getRepository().deleteById(id);
            HookHelper.executeHook(this, id, AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
    default void addBeforeSearchHook(Function<SearchCriteria, SearchCriteria> function) {
    	HookHelper.getServiceHooks(this).get(BEFORE_SEARCH).add(function);
    }
    
    default void addAfterSearchHook(Function<Page<ENTITY_TYPE>, Page<ENTITY_TYPE>> function) {
    	HookHelper.getServiceHooks(this).get(AFTER_SEARCH).add(function);
    }
    
    default void addBeforeReadHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(BEFORE_READ).add(function);
    }
    
    default void addAfterReadHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(AFTER_READ).add(function);
    }
    
    default void addBeforeCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(BEFORE_CREATE).add(function);
    }
    
    default void addAfterCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(AFTER_CREATE).add(function);
    }
    
    default void addBeforeUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(BEFORE_UPDATE).add(function);
    }
    
    default void addAfterUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(AFTER_UPDATE).add(function);
    }
    
    default void addBeforeDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(BEFORE_DELETE).add(function);
    }
    
    default void addAfterDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
    	HookHelper.getServiceHooks(this).get(AFTER_DELETE).add(function);
    }

    default void clearHooks(HookHelper.LifeCycleHook... hookTypes) {
        HookHelper.clearHooks(this, hookTypes);
    }

    static class HookHelper {

        private HookHelper() {}
    	
    	private static Map<CrudService, Map<LifeCycleHook, List<Function>>> GLOBAL_HOOKS = new HashMap<>();
    	
    	private static <S extends CrudService, P> P executeHook(S service, P param, LifeCycleHook hookType) throws Throwable {
            
            List<Function> hooks = getHook(service, hookType);

            param = (P) hooks.stream()
                    .reduce(Function.identity(), Function::andThen)
                    .apply(param);

            return param;
            
        }

        private static <S extends CrudService> List<Function> getHook(S service, LifeCycleHook hookType) {
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

        private static <T extends CrudService> void clearHooks(T service, LifeCycleHook... hookTypes) {
    	    for (LifeCycleHook hook : hookTypes) {
    	        Map<LifeCycleHook, List<Function>> hooks = GLOBAL_HOOKS.get(service);

    	        if (hooks != null)
    	            hooks.get(hook).clear();
            }
        }

        static enum LifeCycleHook {
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
    }
    
}
