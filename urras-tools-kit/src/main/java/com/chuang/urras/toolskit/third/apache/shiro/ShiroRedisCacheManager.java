package com.chuang.urras.toolskit.third.apache.shiro;

import com.chuang.urras.toolskit.third.redis.RedisCache;
import com.chuang.urras.toolskit.third.redis.RedisHCached;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

/**
 * redis的缓存管理器
 *
 */
public class ShiroRedisCacheManager extends AbstractCacheManager {
	private final RedisHCached cached;

	public ShiroRedisCacheManager(RedisHCached cached) {
		this.cached = cached;
	}

    @Override
	@SuppressWarnings("unchecked")
	protected Cache createCache(String cacheName) throws CacheException {
		return new ShiroRedisCache(new RedisCache(cacheName, cached));
	}

}
