package com.chuang.urras.web.office.configuration;

import com.chuang.urras.crud.handlers.ValueGetter;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.web.office.SessionKeys;
import com.chuang.urras.web.office.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Aspect
@Configuration
@Slf4j
public class OperatorInterceptor {

    @Resource
    private HttpServletRequest request;

    @Resource
    @Qualifier("operatorGetter")
    private ValueGetter<String> operatorGetter;


    @Around("execution(public * *(..)) && (" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping))")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable {

        User user = (User) SecurityUtils.getSubject().getSession().getAttribute(SessionKeys.LOGIN_USER);
        if(null != user) {
            operatorGetter.set(user.getUsername());
        } else {
            String operator = request.getParameter("operator");
            if (null == operator) {
                operator = request.getHeader("operator");
            }
            if (StringKit.isNotEmpty(operator)) {
                operatorGetter.set(operator);
            }
        }

        return pjp.proceed();

    }
}
