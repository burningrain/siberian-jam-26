package com.github.br.libgdx.common.structure.artemis;

import com.artemis.World;
import com.artemis.injection.FieldHandler;
import com.artemis.injection.Injector;

import java.util.Map;

public class EmptyInjector implements Injector {

    public static final EmptyInjector INSTANCE = new EmptyInjector();

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

    }

    @Override
    public void inject(Object target) throws RuntimeException {

    }

    @Override
    public boolean isInjectable(Object target) {
        return false;
    }

    @Override
    public Injector setFieldHandler(FieldHandler fieldHandler) {
        return null;
    }
}
