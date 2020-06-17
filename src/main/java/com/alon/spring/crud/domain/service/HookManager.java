package com.alon.spring.crud.domain.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class HookManager {

    private HookManager() {}

    protected static Map<Hookable, Map<LifeCycleHook, List<Function>>> GLOBAL_HOOKS = new HashMap<>();

    protected static <S extends Hookable, P> P executeHook(S service, P param, LifeCycleHook hookType) {
        List<Function> hooks = getHooks(service, hookType);

        param = (P) hooks.stream()
                .reduce(Function.identity(), Function::andThen)
                .apply(param);

        return param;
    }

    protected static <S extends Hookable> void addHook(S hookable, LifeCycleHook hookType, Function hook) {
        getServiceHooks(hookable).get(hookType).add(hook);
    }

    protected static <S extends Hookable> List<Function> getHooks(S service, LifeCycleHook hookType) {
        return getServiceHooks(service).get(hookType);
    }

    protected static <S extends Hookable> Map<LifeCycleHook, List<Function>> getServiceHooks(S service) {
        Map<LifeCycleHook, List<Function>> hooks = GLOBAL_HOOKS.get(service);

        if (hooks == null)
            hooks = initHooks(service);

        return hooks;
    }

    protected static <T extends Hookable> Map<LifeCycleHook, List<Function>> initHooks(T service) {
        Map<LifeCycleHook, List<Function>> hooks = new HashMap<>();

        for (LifeCycleHook hook : LifeCycleHook.values())
            hooks.put(hook, new ArrayList<>());

        GLOBAL_HOOKS.put(service, hooks);

        return hooks;
    }

    protected static <T extends Hookable> void clearHooks(T service, LifeCycleHook... hookTypes) {
        for (LifeCycleHook hook : hookTypes) {
            Map<LifeCycleHook, List<Function>> hooks = GLOBAL_HOOKS.get(service);

            if (hooks != null)
                hooks.get(hook).clear();
        }
    }
}
