package com.chuang.urras.web.office;

import org.apache.shiro.ShiroException;

public class PrincipalExpiredException extends ShiroException {
    public PrincipalExpiredException() {
        super("");
    }
}
