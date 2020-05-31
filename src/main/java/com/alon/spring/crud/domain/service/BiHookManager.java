package com.alon.spring.crud.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class BiHookManager {

    private BiHookManager() {}

    protected static Map<BiHookable, Map<LifeCycleHook, List<BiFunction>>> GLOBAL_HOOKS = new HashMap<>();

    protected static <S extends BiHookable, PA, PB> PA executeHook(
            S service, LifeCycleHook hookType, PA paramA, PB paramB) {

        List<BiFunction> hooks = getHooks(service, hookType);

        for (BiFunction hook : hooks) {
            paramA = (PA) hook.apply(paramA, paramB);
        }

        return paramA;
    }

    protected static <S extends BiHookable> void addHook(S hookable, LifeCycleHook hookType, BiFunction hook) {
        getServiceHooks(hookable).get(hookType).add(hook);
    }

    protected static <S extends BiHookable> List<BiFunction> getHooks(S service, LifeCycleHook hookType) {
        return getServiceHooks(service).get(hookType);
    }

    protected static <S extends BiHookable> Map<LifeCycleHook, List<BiFunction>> getServiceHooks(S service) {
        Map<LifeCycleHook, List<BiFunction>> hooks = GLOBAL_HOOKS.get(service);

        if (hooks == null)
            hooks = initHooks(service);

        return hooks;
    }

    protected static <T extends BiHookable> Map<LifeCycleHook, List<BiFunction>> initHooks(T service) {
        Map<LifeCycleHook, List<BiFunction>> hooks = new HashMap<>();

        for (LifeCycleHook hook : LifeCycleHook.values())
            hooks.put(hook, new ArrayList<>());

        GLOBAL_HOOKS.put(service, hooks);

        return hooks;
    }

    protected static <T extends BiHookable> void clearHooks(T service, LifeCycleHook... hookTypes) {
        for (LifeCycleHook hook : hookTypes) {
            Map<LifeCycleHook, List<BiFunction>> hooks = GLOBAL_HOOKS.get(service);

            if (hooks != null)
                hooks.get(hook).clear();
        }
    }
}
