package com.chuang.urras.web.shiro.properties;


import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "urras.shiro.session")
public class SessionProperties {

    private String cookieName = "urras-cookie-token";
    private boolean httpOnly = false;
    private int maxAge = 180000;
    private boolean cookieEnable = true;
    private int globalTimeout = 1800000;
    private boolean deleteInvalidSessions = true;
    private boolean validationSchedulerEnabled = true;
    private long validationInterval = 3600000;
    private String activeSessionCacheName= CachingSessionDAO.ACTIVE_SESSION_CACHE_NAME;

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public boolean isCookieEnable() {
        return cookieEnable;
    }

    public void setCookieEnable(boolean cookieEnable) {
        this.cookieEnable = cookieEnable;
    }

    public int getGlobalTimeout() {
        return globalTimeout;
    }

    public void setGlobalTimeout(int globalTimeout) {
        this.globalTimeout = globalTimeout;
    }

    public boolean isDeleteInvalidSessions() {
        return deleteInvalidSessions;
    }

    public void setDeleteInvalidSessions(boolean deleteInvalidSessions) {
        this.deleteInvalidSessions = deleteInvalidSessions;
    }

    public boolean isValidationSchedulerEnabled() {
        return validationSchedulerEnabled;
    }

    public void setValidationSchedulerEnabled(boolean validationSchedulerEnabled) {
        this.validationSchedulerEnabled = validationSchedulerEnabled;
    }

    public long getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(long validationInterval) {
        this.validationInterval = validationInterval;
    }


    public String getActiveSessionCacheName() {
        return activeSessionCacheName;
    }

    public void setActiveSessionCacheName(String activeSessionCacheName) {
        this.activeSessionCacheName = activeSessionCacheName;
    }
}
