package com.chuang.urras.web.office;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PrincipalExpireInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (null != request.getSession().getAttribute(SessionKeys.KEY_PRINCIPAL_EXPIRED)) {
            request.getSession().removeAttribute(SessionKeys.KEY_PRINCIPAL_EXPIRED);
           throw new PrincipalExpiredException();
        }

        return true;
    }
}
