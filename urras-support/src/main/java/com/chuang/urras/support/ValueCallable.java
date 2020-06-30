package com.chuang.urras.support;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public class ValueCallable<V> {
    private final V value;
    private final Runnable whenValueUsed;

    public ValueCallable(@Nullable V value, @Nullable Runnable whenValueUsed) {
        this.value = value;
        this.whenValueUsed = whenValueUsed;
    }

    public void useValue(Consumer<V> consumer) {
        consumer.accept(value);
        if (null != whenValueUsed)  whenValueUsed.run();
    }

    public <R> R getValue(Function<V, R> function) {
        R r = function.apply(value);
        if (null != whenValueUsed) whenValueUsed.run();
        return r;
    }
}
