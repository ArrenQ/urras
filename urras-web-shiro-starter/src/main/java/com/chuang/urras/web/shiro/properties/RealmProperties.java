package com.chuang.urras.web.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *      * @param cacheEnable 是否开启缓存
 *      * @param authenticationCacheEnable 是否开启令牌缓存
 *      * @param authenticationCacheName 令牌缓存名称
 *      * @param authorizationEnable 是否开启权限缓存
 *      * @param authorizationCacheName 权限缓存名称
 *
 */
@Component
@ConfigurationProperties(prefix = "urras.shiro.realm")
public class RealmProperties {

    private boolean cacheEnabled = false;
    private boolean authenticationCacheEnabled = false;
    private String authenticationCacheName;
    private boolean authorizationCacheEnabled = false;
    private String authorizationCacheName;

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isAuthenticationCacheEnabled() {
        return authenticationCacheEnabled;
    }

    public void setAuthenticationCacheEnabled(boolean authenticationCacheEnabled) {
        this.authenticationCacheEnabled = authenticationCacheEnabled;
    }

    public String getAuthenticationCacheName() {
        return authenticationCacheName;
    }

    public void setAuthenticationCacheName(String authenticationCacheName) {
        this.authenticationCacheName = authenticationCacheName;
    }

    public boolean isAuthorizationCacheEnabled() {
        return authorizationCacheEnabled;
    }

    public void setAuthorizationCacheEnabled(boolean authorizationCacheEnabled) {
        this.authorizationCacheEnabled = authorizationCacheEnabled;
    }

    public String getAuthorizationCacheName() {
        return authorizationCacheName;
    }

    public void setAuthorizationCacheName(String authorizationCacheName) {
        this.authorizationCacheName = authorizationCacheName;
    }


}
