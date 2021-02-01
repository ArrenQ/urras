package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.Language;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.web.office.SessionKeys;
import com.chuang.urras.web.office.exception.CaptchaException;
import com.chuang.urras.web.office.exception.CaptchaTimeOutException;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.model.UserPrincipal;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.CompletionException;

/**
 * Created by ath on 2016/3/2.
 */
public class BaseController {
    private final static long CAPTCHA_TIMEOUT = 60000;
    protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Locale DEFAULT_LOCALE = new Locale("zh", "CN");

    @Resource
    private MessageSource messageSource;

    @Resource
    private SessionDAO sessionDAO;

    /**
     * 验证码校验
     * 验证码超时时间 1 分钟{@link #CAPTCHA_TIMEOUT}
     */
    public boolean verifyCaptcha(String captcha, boolean delete) {
        Session curSession = SecurityUtils.
                getSubject().getSession();
        String exitCode = (String) curSession.
                getAttribute(SessionKeys.KEY_CAPTCHA);

        Long captchaTime = (Long) curSession.
                getAttribute(SessionKeys.KEY_CAPTCHA_TIME);
        if (delete) {
            curSession.removeAttribute(SessionKeys.KEY_CAPTCHA);
            curSession.removeAttribute(SessionKeys.KEY_CAPTCHA_TIME);
        }
        if (StringKit.isBlank(exitCode) || null == captchaTime) {
            throw new CaptchaException(getMessage("user.captcha.is.null"));
        }
        if (new Date().getTime() - captchaTime > CAPTCHA_TIMEOUT) {
            throw new CaptchaTimeOutException(getMessage("user.captcha.timeout"));
        }
        if (!captcha.equalsIgnoreCase(exitCode)) {
            throw new CaptchaException(getMessage("user.verific.ationcode.error"));
        }
        return true;

    }

    protected String getMessage(String key, Object... args) {
        Locale locale = (Locale) getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        if(null == locale) {
            locale = DEFAULT_LOCALE;
        }
        return messageSource.getMessage(key, args, locale);
    }
//    /**
//     * 获取当前令牌
//     * @return
//     */
//    public UserPrincipalEntity getCurrentPrincipal() {
//        return (UserPrincipalEntity)SecurityUtils.getSubject().getPrincipal();
//    }
    @SuppressWarnings("unchecked")
    public Set<UserPrincipal> getCurrentPrincipals() {
        return (Set<UserPrincipal>)SecurityUtils.getSubject().getPrincipals().asSet();
    }



    /**
     * 获取登录用户，注意，如果需要用户的资料是和权限业务相关，请考虑是否应该使用 getCurrentPrincipal()
     */
    public User getLoginUser() {
        return (User) getSession().getAttribute(SessionKeys.LOGIN_USER);
    }



    public Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

    public Result createErrorResult(Throwable e) {
        return createErrorResult(e, "系统发生异常");
    }
    public Result createErrorResult(Throwable e, String logText) {

        if(e instanceof CompletionException && e.getCause() instanceof SystemWarnException) {
            e = e.getCause();
        }

        if(e instanceof SystemWarnException || e instanceof org.apache.shiro.authc.AuthenticationException) {
            return Result.fail(e.getMessage());
        }

        String err = "Ex" + UUID.randomUUID().toString();

        logger.error(logText + ", 错误号:" + err + ", 用户:" + getLoginUser().getUsername() , e);
        return Result.fail("系统异常,请联系客服!异常号:" + err);
    }
    
    protected void changeLanguage(Language language) {
        if(Language.ZH_CN == language){
            getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale("zh", "CN"));
        } else if (Language.ZH_TW == language) {
            getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale("zh", "TW"));
        } else if(Language.EN_US == language) {
            getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, new Locale("en", "US"));
        }
    }

    protected void tickUser(Collection<String> usernames) {
        if(!usernames.isEmpty()) {
            Collection<Session> sessions = sessionDAO.getActiveSessions();
            for (Session session : sessions) {
                User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
                if (null != user && usernames.contains(user.getUsername())) {
                   sessionDAO.delete(session);
                    break;
                }
            }
        }
    }

    protected void notifyRefreshInfo(Collection<String> usernames) {
        if(!usernames.isEmpty()) {
            Collection<Session> sessions = sessionDAO.getActiveSessions();
            for (Session session : sessions) {
                User user = (User) session.getAttribute(SessionKeys.LOGIN_USER);
                if (null != user && usernames.contains(user.getUsername())) {
                    session.setAttribute(SessionKeys.KEY_PRINCIPAL_EXPIRED, true);
                    sessionDAO.update(session);
                    break;
                }
            }
        }
    }
}
