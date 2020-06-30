package com.chuang.urras.toolskit.third.apache.shiro;

import com.chuang.urras.toolskit.third.redis.RedisCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Shiro 实现的缓存
 *
 * @param <K>
 * @param <V>
 */
public class ShiroRedisCache<K,V> implements Cache<K,V> {
	private com.chuang.urras.toolskit.basic.cache.Cache<K, V> impl;

	private static final Logger log = LoggerFactory.getLogger(ShiroRedisCache.class);

	public ShiroRedisCache(RedisCache<K, V> redisImpl){
		this.impl = redisImpl;
	}

	@Override
	public V get(K key) throws CacheException {
		try {
			return impl.get(key).orElse(null);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public V put(K key, V value) throws CacheException {
		if(log.isDebugEnabled()) {
			log.debug("shiro redis cache put [{}:{}]", key, value);
		}
		try {
		    Optional<V> old = impl.get(key);
			impl.put(key, value);
			return old.orElse(null);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public V remove(K key) throws CacheException {
		if(log.isDebugEnabled()) {
			log.debug("shiro redis cache remove {}", key);
		}
		try {
            Optional<V> old = impl.get(key);
			impl.remove(key);
			return old.orElse(null);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void clear() throws CacheException {
		log.debug("shiro redis cache clear");

		try {
			impl.clear();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public int size() {
		try {
			return impl.size();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Set<K> keys() {
		try {
			return impl.keys();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Collection<V> values() {
		try {
			return impl.values();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

}
