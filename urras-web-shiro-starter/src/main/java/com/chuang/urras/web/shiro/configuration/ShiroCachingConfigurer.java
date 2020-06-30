package com.chuang.urras.web.shiro.configuration;

import com.chuang.urras.toolskit.third.redis.RedisHCached;
import com.chuang.urras.toolskit.third.apache.shiro.ShiroRedisCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by ath on 2017/3/29.
 */
@Configuration
public class ShiroCachingConfigurer {
//    @Bean
//    public ShiroRedisCachingProperties shiroRedisCachingProperties() {
//        return new ShiroRedisCachingProperties();
//    }
//
//
//    public LettuceConnectionFactory shiroJedisConnectionFactory() {
//        ShiroRedisCachingProperties shiroRedisCachingProperties = shiroRedisCachingProperties();
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setDatabase(shiroRedisCachingProperties.getDbIndex());
//        configuration.setHostName(shiroRedisCachingProperties.getHost());
//        configuration.setPassword(RedisPassword.of(shiroRedisCachingProperties.getPassword()));
//        configuration.setPort(shiroRedisCachingProperties.getPort());
//        return new LettuceConnectionFactory(configuration, poolConfig());
//    }
//
//
//    public LettucePoolingClientConfiguration poolConfig() {
//        ShiroRedisCachingProperties shiroRedisCachingProperties = shiroRedisCachingProperties();
//        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
//        config.setMaxIdle(shiroRedisCachingProperties.getMaxIdle());
//        config.setTestOnBorrow(shiroRedisCachingProperties.isTestOnBorrow());
//        config.setMaxTotal(shiroRedisCachingProperties.getMaxTotal());
//        config.setMaxWaitMillis(shiroRedisCachingProperties.getMaxWaitMillis());
//        return LettucePoolingClientConfiguration.builder().poolConfig(config).build();
//    }


    /**
     * 缓存管理器，这里securityManager，sessionManager都使用同一个，用不同的name
     * @return
     */
    @Bean("shiroCacheManager")
    public ShiroRedisCacheManager cacheManager(RedisHCached redisHCached) {
        return new ShiroRedisCacheManager(redisHCached);
    }

    /**
     * expire字段会设置整个shiro sessionCache的过期时间。只要内部创建时使用updateCached 方法就能实现。
     * 但这里并没有使用，删除也无妨。session的管理全权交由shiro. shiro session使用定时检查过期的session。
     * @return
     */
    @Bean("shiroRedisCache")
    public RedisHCached shiroRedisCached(RedisTemplate redisTemplate) {
        RedisHCached redisCached = new RedisHCached();
        redisCached.setRedisTemplate(redisTemplate);
        return redisCached;
    }

}
