package com.alon.spring.crud.domain.service;

import org.springframework.data.domain.Page;

import java.util.function.Function;

import static com.alon.spring.crud.domain.service.HookManager.*;
import static com.alon.spring.crud.domain.service.LifeCycleHook.*;
import static com.alon.spring.crud.domain.service.LifeCycleHook.AFTER_DELETE;

public interface Hookable<ENTITY_ID_TYPE, ENTITY_TYPE> {
    default void addBeforeSearchHook(Function<SearchCriteria, SearchCriteria> function) {
        addHook(this, BEFORE_SEARCH, function);
    }

    default void addAfterSearchHook(Function<Page<ENTITY_TYPE>, Page<ENTITY_TYPE>> function) {
        addHook(this, AFTER_SEARCH, function);
    }

    default void addBeforeReadHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
        addHook(this, BEFORE_READ, function);
    }

    default void addAfterReadHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
        addHook(this, AFTER_READ, function);
    }

    default void addBeforeCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
        addHook(this, BEFORE_CREATE, function);
    }

    default void addAfterCreateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
        addHook(this, AFTER_CREATE, function);
    }

    default void addBeforeUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
        addHook(this, BEFORE_UPDATE, function);
    }

    default void addAfterUpdateHook(Function<ENTITY_TYPE, ENTITY_TYPE> function) {
        addHook(this, AFTER_UPDATE, function);
    }

    default void addBeforeDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
        addHook(this, BEFORE_DELETE, function);
    }

    default void addAfterDeleteHook(Function<ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {
        addHook(this, AFTER_DELETE, function);
    }

    default SearchCriteria executeBeforeSearchHooks(SearchCriteria entity) {
        return executeHook(this, entity, BEFORE_SEARCH);
    }

    default Page<ENTITY_TYPE> executeAfterSearchHooks(Page<ENTITY_TYPE> page) {
        return executeHook(this, page, AFTER_SEARCH);
    }

    default ENTITY_ID_TYPE executeBeforeReadHooks(ENTITY_ID_TYPE id) {
        return executeHook(this, id, BEFORE_READ);
    }

    default ENTITY_TYPE executeAfterReadHooks(ENTITY_TYPE entity) {
        return executeHook(this, entity, AFTER_READ);
    }

    default ENTITY_TYPE executeBeforeCreateHooks(ENTITY_TYPE entity) {
        return executeHook(this, entity, BEFORE_CREATE);
    }

    default ENTITY_TYPE executeAfterCreateHooks(ENTITY_TYPE entity) {
        return executeHook(this, entity, AFTER_CREATE);
    }

    default ENTITY_TYPE executeBeforeUpdateHooks(ENTITY_TYPE entity) {
        return executeHook(this, entity, BEFORE_UPDATE);
    }

    default ENTITY_TYPE executeAfterUpdateHooks(ENTITY_TYPE entity) {
        return executeHook(this, entity, AFTER_UPDATE);
    }

    default ENTITY_ID_TYPE executeBeforeDeleteHooks(ENTITY_ID_TYPE id) {
        return executeHook(this, id, BEFORE_DELETE);
    }

    default ENTITY_ID_TYPE executeAfterDeleteHooks(ENTITY_ID_TYPE id) {
        return executeHook(this, id, AFTER_DELETE);
    }

    default void clearHooks(LifeCycleHook... hookTypes) {
        HookManager.clearHooks(this, hookTypes);
    }
}
