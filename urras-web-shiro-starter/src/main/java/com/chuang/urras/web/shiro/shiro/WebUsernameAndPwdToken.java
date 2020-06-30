package com.chuang.urras.web.shiro.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Created by ath on 2016/12/28.
 */
public class WebUsernameAndPwdToken extends UsernamePasswordToken {

    private final String userAgent, referer;
    private Integer siteId;

    public WebUsernameAndPwdToken(final String username, final char[] password,
                                  final String host, String userAgent, String referer) {
        super(username, password, host);
        this.userAgent = userAgent;
        this.referer = referer;
    }

    public WebUsernameAndPwdToken(final String username, final String password,
                                  final String host, String userAgent, String referer) {
        super(username, password, host);
        this.userAgent = userAgent;
        this.referer = referer;
    }

    public WebUsernameAndPwdToken(final String username, final char[] password,
                                  final boolean rememberMe, final String host, String userAgent, String referer) {
        super(username, password, rememberMe, host);
        this.userAgent = userAgent;
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public Integer getSiteId() { return siteId; }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
}
