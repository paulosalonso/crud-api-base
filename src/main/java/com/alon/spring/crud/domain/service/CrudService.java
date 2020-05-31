package com.alon.spring.crud.domain.service;

import com.alon.spring.crud.domain.model.BaseEntity;
import com.alon.spring.crud.domain.repository.CrudRepository;
import com.alon.spring.crud.domain.service.exception.*;
import com.cosium.spring.data.jpa.entity.graph.domain.DynamicEntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static com.alon.spring.crud.domain.service.LifeCycleHook.*;

public interface CrudService<
        ENTITY_ID_TYPE extends Serializable, 
        ENTITY_TYPE extends BaseEntity<ENTITY_ID_TYPE>, 
        REPOSITORY extends CrudRepository<ENTITY_ID_TYPE, ENTITY_TYPE>>
extends Hookable<ENTITY_ID_TYPE, ENTITY_TYPE> {
    
    REPOSITORY getRepository();

    default Page<ENTITY_TYPE> search(SearchCriteria criteria) {
    	
        try {
            HookManager.executeHook(this, criteria, BEFORE_SEARCH);

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

            HookManager.executeHook(this, searchResult, AFTER_SEARCH);

            return searchResult;
        } catch (Throwable ex) {
            String message = String.format("Error searching entities: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }

    }

    default ENTITY_TYPE create(@Valid ENTITY_TYPE entity) {
        try {
            entity = HookManager.executeHook(this, entity, BEFORE_CREATE);
            entity = (ENTITY_TYPE) getRepository().save(entity);
            return HookManager.executeHook(this, entity, AFTER_CREATE);
        } catch (Throwable ex) {
            throw new CreateException(ex.getMessage(), ex);
        }
    }

    default ENTITY_TYPE read(ENTITY_ID_TYPE id) {
        return this.read(id, null);
    }

    default ENTITY_TYPE read(ENTITY_ID_TYPE id, List<String> expand) {
        try {
            id = HookManager.executeHook(this, id, BEFORE_READ);

            Optional<ENTITY_TYPE> opt;

            if (expand != null && !expand.isEmpty())
                opt = getRepository().findById(id, new DynamicEntityGraph(expand));
            else
                opt = getRepository().findById(id);

            if (opt.isEmpty())
                throw new NotFoundException(String.format("ID not found -> %d", id));

            ENTITY_TYPE entity = opt.get();

            entity = HookManager.executeHook(this, entity, AFTER_READ);

            return entity;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Throwable ex) {
            String message = String.format("Error reading entity: %s", ex.getMessage());
            throw new ReadException(message, ex);
        }
    }

    default ENTITY_TYPE update(@Valid ENTITY_TYPE entity) {
        if (!getRepository().existsById(entity.getId()))
            throw new NotFoundException("Entity to update not found");

        try {
            entity = HookManager.executeHook(this, entity, BEFORE_UPDATE);
            entity = (ENTITY_TYPE) this.getRepository().save(entity);
            return HookManager.executeHook(this, entity, AFTER_UPDATE);
        } catch (Throwable ex) {
            throw new UpdateException(ex.getMessage(), ex);
        }
    }

    default void delete(ENTITY_ID_TYPE id) {
        if (!getRepository().existsById(id))
            throw new NotFoundException(String.format("ID not found -> %d", id));

        try {
        	HookManager.executeHook(this, id, BEFORE_DELETE);
            getRepository().deleteById(id);
            HookManager.executeHook(this, id, AFTER_DELETE);
        } catch (Throwable ex) {
            throw new DeleteException(ex.getMessage(), ex);
        }
    }
    
}
