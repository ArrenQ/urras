package com.chuang.urras.web.shiro.configuration;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyShiroSessionManager extends DefaultWebSessionManager {
    private final Logger logger = LoggerFactory.getLogger(MyShiroSessionManager.class);
    private final Lock lock = new ReentrantLock();
    /**
     * 获取session
     * 优化单次请求需要多次访问redis的问题.
     * 问题描述，shiro这老兄每次获取session都会通过retrieveSession（cacheManager）去获取。但每一次请求发生时，一堆拦截器都会去getSession()
     * 这导致每一次请求，shiro都丧心病狂的疯狂访问redis。因此需要对这个做优化。
     * 暂时不打算考虑在{@link ShiroRedisCache} 中加入ThreadLocal来缓存，
     * 因为session有自己的生命周期，在某些情况下session被容器回收。但线程对象只有在线程注销时才被清理（如果容器是使用线程池则是被下一次请求覆盖）。
     * 这样做可能导致session本身被容器回收后，仍然有可能获取到。虽然 ShiroRedisCache 和方法是和shiro管理session的周期是一致的，我们也可以在里面进行处理
     * 但不如修改这里来得简单。
     * 另外这种做法对 shiro + druid 产生的问题有神奇的疗效。
     */
    @Override
    protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {

        Serializable sessionId = getSessionId(sessionKey);
        if (sessionId == null) {
            logger.debug("Unable to resolve session ID from SessionKey [{}].  Returning null to indicate a " +
                    "session could not be found.", sessionKey);
            return null;
        }

        //=== start 在父类实现基础上，添加这段代码，默认先获取request中的session。 request获取不到才通过retrieveSession到 cacheManager(redis) 中获取。
        ServletRequest request = null;
        if (sessionKey instanceof WebSessionKey) {
            request = ((WebSessionKey) sessionKey).getServletRequest();
        }
        if (request != null) {
            Object sessionObj = request.getAttribute(sessionId.toString());
            if (sessionObj != null) {
                return (Session) sessionObj;
            }
        }
        //=== end

        Session session = super.retrieveSession(sessionKey);
        if (request != null) {
            request.setAttribute(sessionId.toString(), session);
        }
        return session;
    }

    /**
     * 开启session validation的方法，父类实际上已经实现，但是开启session validation定时任务是延迟开启的，而且没有做同步处理。
     * 可能会导致系统启动时N个request同时请求，导致定时任务被多次开启。
     * 本系统使用了 swagger，而swagger bean的加载都是在http端口启动后才进行的。swagger加载前，所以的request都会被阻塞。
     * 如果swagger加载太慢会导致大量request堆积，并同时开始调用 enableSessionValidationIfNecessary ，
     * 而enableSessionValidationIfNecessary 内部调用 enableSessionValidation 最终同时并发创建了大量相同的定时任务。
     *
     * 该方法如果创建定时任务成功， enableSessionValidationIfNecessary 方法不会再次对它进行调用。所以内部加锁并不影响后续的操作。
     */
    @Override
    protected void enableSessionValidation() {
        logger.info("试图启动定时任务.......");
        if(lock.tryLock()) {
            try {
                SessionValidationScheduler scheduler = getSessionValidationScheduler();
                if (isSessionValidationSchedulerEnabled() && (scheduler == null || !scheduler.isEnabled())) {
                    super.enableSessionValidation();
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
