package com.alon.spring.crud.domain.service;

import org.springframework.data.domain.Page;

import java.util.function.BiFunction;

import static com.alon.spring.crud.domain.service.BiHookManager.addHook;
import static com.alon.spring.crud.domain.service.BiHookManager.executeHook;
import static com.alon.spring.crud.domain.service.LifeCycleHook.*;

public interface BiHookable<MASTER_ENTITY_ID_TYPE, ENTITY_ID_TYPE, ENTITY_TYPE> {

    default void addBeforeSearchHook(
            BiFunction<SearchCriteria, MASTER_ENTITY_ID_TYPE, SearchCriteria> function) {

        addHook(this, BEFORE_SEARCH, function);
    }

    default void addAfterSearchHook(
            BiFunction<Page<ENTITY_TYPE>, MASTER_ENTITY_ID_TYPE, Page<ENTITY_TYPE>> function) {

        addHook(this, AFTER_SEARCH, function);
    }

    default void addBeforeReadHook(
            BiFunction<ENTITY_ID_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {

        addHook(this, BEFORE_READ, function);
    }

    default void addAfterReadHook(
            BiFunction<ENTITY_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_TYPE> function) {

        addHook(this, AFTER_READ, function);
    }

    default void addBeforeCreateHook(
            BiFunction<ENTITY_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_TYPE> function) {

        addHook(this, BEFORE_CREATE, function);
    }

    default void addAfterCreateHook(
            BiFunction<ENTITY_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_TYPE> function) {

        addHook(this, AFTER_CREATE, function);
    }

    default void addBeforeUpdateHook(
            BiFunction<ENTITY_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_TYPE> function) {

        addHook(this, BEFORE_UPDATE, function);
    }

    default void addAfterUpdateHook(
            BiFunction<ENTITY_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_TYPE> function) {

        addHook(this, AFTER_UPDATE, function);
    }

    default void addBeforeDeleteHook(
            BiFunction<ENTITY_ID_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {

        addHook(this, BEFORE_DELETE, function);
    }

    default void addAfterDeleteHook(
            BiFunction<ENTITY_ID_TYPE, MASTER_ENTITY_ID_TYPE, ENTITY_ID_TYPE> function) {

        addHook(this, AFTER_DELETE, function);
    }

    default SearchCriteria executeBeforeSearchHooks(SearchCriteria search, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, BEFORE_SEARCH, search, masterId);
    }

    default Page<ENTITY_TYPE> executeAfterSearchHooks(Page<ENTITY_TYPE> page, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, AFTER_SEARCH, page, masterId);
    }

    default ENTITY_ID_TYPE executeBeforeReadHooks(ENTITY_ID_TYPE id, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, BEFORE_READ, id, masterId);
    }

    default ENTITY_TYPE executeAfterReadHooks(ENTITY_TYPE entity, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, AFTER_READ, entity, masterId);
    }

    default ENTITY_TYPE executeBeforeCreateHooks(ENTITY_TYPE entity, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, BEFORE_CREATE, entity, masterId);
    }

    default ENTITY_TYPE executeAfterCreateHooks(ENTITY_TYPE entity, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, AFTER_CREATE, entity, masterId);
    }

    default ENTITY_TYPE executeBeforeUpdateHooks(ENTITY_TYPE entity, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, BEFORE_UPDATE, entity, masterId);
    }

    default ENTITY_TYPE executeAfterUpdateHooks(ENTITY_TYPE entity, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, AFTER_UPDATE, entity, masterId);
    }

    default ENTITY_ID_TYPE executeBeforeDeleteHooks(ENTITY_ID_TYPE id, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, BEFORE_DELETE, id, masterId);
    }

    default ENTITY_ID_TYPE executeAfterDeleteHooks(ENTITY_ID_TYPE id, MASTER_ENTITY_ID_TYPE masterId) {
        return executeHook(this, AFTER_DELETE, id, masterId);
    }

    default void clearHooks(LifeCycleHook... hookTypes) {
        BiHookManager.clearHooks(this, hookTypes);
    }

}
