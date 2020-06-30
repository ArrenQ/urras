package com.chuang.urras.crud.handlers;

import java.util.Optional;

public class ThreadLocalValueGetter<T> implements ValueGetter<T> {
    private ThreadLocal<T> objectThreadLocal = new ThreadLocal<>();
    @Override
    public Optional<T> get() {
        T obj = objectThreadLocal.get();
        return Optional.ofNullable(obj);
    }

    public void set(T obj) {
        this.objectThreadLocal.set(obj);
    }

    @Override
    public void release() {
        this.objectThreadLocal.set(null);
    }
}
