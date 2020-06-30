package com.chuang.urras.toolskit.basic.cache;


import java.util.Collection;
import java.util.Optional;

public interface ExpireCache<V> extends Cache<String, V> {

    Optional<V> put(String key, V value, long expireMS) throws Exception ;

    Collection<V> getByPattern(String pattern);


    Optional<Long> removeByPattern(String pattern);
}
