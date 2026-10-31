package com.artemis.injection;

import com.artemis.World;

import java.util.Map;

public class CachedInjector implements Injector {

    @Override
    public <T> T getRegistered(String id) {
        return null;
    }

    @Override
    public <T> T getRegistered(Class<T> id) {
        return null;
    }

    @Override
    public void initialize(World world, Map<String, Object> injectables) {
        // Ничего не делаем
    }

    @Override
    public void inject(Object target) throws RuntimeException {
        // Ничего не делаем
    }

    @Override
    public boolean isInjectable(Object target) {
        return false;
    }

    @Override
    public Injector setFieldHandler(FieldHandler fieldHandler) {
        return this;
    }

}
