package com.chuang.urras.toolskit.third.redis;

import com.chuang.urras.toolskit.basic.BasicKit;
import com.chuang.urras.toolskit.basic.cache.ExpireCache;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisExpireCache<V> implements ExpireCache<V> {

    private final String name;
    private final RedisHCached cached;
    private final RedisTemplate<String, Object> template;

    public RedisExpireCache(String name, RedisHCached cached){
        this.name=name;
        this.cached=cached;
        this.template = cached.getRedisTemplate();
    }

    private String prefix() {
        return name + ":";
    }
    /**
     * 获得byte[]型的key
     * @param key
     * @return
     */
    private byte[] getByteKey(String key){
        return (prefix() + key).getBytes();
    }


    @Override
    @SuppressWarnings("unchecked")
    public Optional<V> put(String key, V value, long expireMS) {
        return template.execute((RedisCallback<Optional<V>>) connection -> {
            connection.multi();
            Optional<V> o = (Optional<V>) BasicKit.unSerialize(connection.get(getByteKey(key)));
            connection.set(getByteKey(key), BasicKit.serialize(value).get());
            connection.pExpire(getByteKey(key), expireMS);
            connection.exec();
            return o;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> getByPattern(String pattern) {
        return (Collection<V>) cached.getFields(getByteKey(pattern));
    }

    @Override
    public Optional<Long> removeByPattern(String pattern) {
        return cached.deletePattern(getByteKey(pattern));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<V> get(String key) {
        return template.execute((RedisCallback<Optional<V>>) connection ->
                (Optional<V>) BasicKit.unSerialize(connection.get(getByteKey(key)))
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Optional<Boolean> put(String key, V value) {
        return Optional.ofNullable(template.execute((RedisCallback<Boolean>) connection -> connection.set(getByteKey(key), BasicKit.serialize(value).get())));
    }

    @Override
    public Optional<Boolean> remove(String key) {
        return cached.delete(getByteKey(key));
    }

    @Override
    public Optional<Boolean> clear() {
        return cached.deletePattern(getByteKey("*")).map(aLong -> aLong > 0);
    }

    @Override
    public int size() {
        return cached.getKeysLen(getByteKey("*"));
    }

    @Override
    public Set<String> keys() {
        return cached.getKeys(getByteKey("*")).stream().map(String::new).collect(Collectors.toSet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return (Collection<V>) cached.getFields(getByteKey("*"));
    }
}
