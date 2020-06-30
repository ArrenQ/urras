package com.chuang.urras.web.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;


@Component
@ConfigurationProperties(prefix = "urras.shiro.auth")
public class AuthProperties {
    private String loginUrl = "/auth/unauthorized";
    private String successUrl = "/";
    private String unauthorizedUrl = "/";

    private LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

    public AuthProperties() {
        filterChainDefinitionMap.put("/swagger*/**", "anon");
        filterChainDefinitionMap.put("/v2/api-docs", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/auth/login", "anon");
        filterChainDefinitionMap.put("/auth/unauthorized", "anon");
        filterChainDefinitionMap.put("/captcha*", "anon");
        filterChainDefinitionMap.put("/actuator/*", "anon");
        filterChainDefinitionMap.put("/logout", "logout");
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getUnauthorizedUrl() {
        return unauthorizedUrl;
    }

    public void setUnauthorizedUrl(String unauthorizedUrl) {
        this.unauthorizedUrl = unauthorizedUrl;
    }

    public LinkedHashMap<String, String> getFilterChainDefinitionMap() {
        return filterChainDefinitionMap;
    }

    public void setFilterChainDefinitionMap(LinkedHashMap<String, String> filterChainDefinitionMap) {
        this.filterChainDefinitionMap = filterChainDefinitionMap;
    }
}
