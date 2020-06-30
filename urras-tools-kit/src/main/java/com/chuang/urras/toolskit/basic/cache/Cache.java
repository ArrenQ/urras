package com.chuang.urras.toolskit.basic.cache;


import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface Cache<K, V> {
    Optional<V> get(K key);

    Optional<Boolean> put(K key, V value) ;

    Optional<Boolean> remove(K key)  ;

    Optional<Boolean> clear() ;

    /**
     * size可能是通过keys().size() 来获取的。
     */
    int size();

    Set<K> keys() ;

    Collection<V> values();
}
