package com.chuang.urras.web.shiro.configuration;

import com.chuang.urras.toolskit.third.apache.shiro.ShiroRedisCacheManager;
import com.chuang.urras.web.shiro.properties.AuthProperties;
import com.chuang.urras.web.shiro.properties.HashedCredentialProperties;
import com.chuang.urras.web.shiro.properties.RealmProperties;
import com.chuang.urras.web.shiro.properties.SessionProperties;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ath on 2017/3/29.
 */
@Configuration
@ComponentScan("com.chuang.urras.web.shiro")
@EnableConfigurationProperties({
        AuthProperties.class,
        HashedCredentialProperties.class,
        RealmProperties.class,
        SessionProperties.class,
//        ShiroRedisCachingProperties.class
})
public class ShiroAutoConfiguration {



    /**
     * 证实现了Shiro内部lifecycle函数的bean执行
     * LifecycleBeanPostProcessor，这是个DestructionAwareBeanPostProcessor的子类，
     * 负责org.apache.shiro.util.Initializable类型bean的生命周期的，初始化和销毁。
     * 主要是AuthorizingRealm类的子类，以及CacheManager类。
     * @return
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 校验器
     * HashedCredentialsMatcher，这个类是为了对密码进行编码的，防止密码在数据库里明码保存，
     * 当然在登陆认证的生活，这个类也负责对form里输入的密码进行编码。
     * @return
     */
    @Bean(name = "hashedCredentialsMatcher")
    @ConditionalOnMissingBean
    public HashedCredentialsMatcher hashedCredentialsMatcher(HashedCredentialProperties hashedCredentialProperties) {
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashIterations(hashedCredentialProperties.getIterations());
        credentialsMatcher.setHashAlgorithmName(hashedCredentialProperties.getAlgorithm());
        credentialsMatcher.setStoredCredentialsHexEncoded(true);
        return credentialsMatcher;
    }

    /**
     * session管理器，使用shiro-web 默认的构建session
     * sessionDAO : session将session进行“持久化”，sessionDAO最终决定session中的信息保存到哪里。
     * cacheManager : 缓存管理器，和  securityManager 使用同一个缓存管理器。
     * sessionIdCookie : 创建会话Cookie的模板
     * sessionValidationScheduler : session有效检查调度
     * （默认如果为null，使用createSessionValidationScheduler() 构建一个）
     * 类型为：JDK 的 ScheduledExecutorService
     * sessionIdCookieEnabled : 是否开启会话模板
     * sessionListeners : session监听器，用于监听session各个事件，实现org.apache.shiro.session.SessionListener接口
     *
     * 如果开启定时检查，会调用enableSessionValidation()开启sessionValidationScheduler定时线程，一段时间内对session进行检查
     * org.apache.shiro.session.mgt.ValidatingSessionManager的validateSessions()方法将调用每个session的validate()方法。
     * 如果检查出session过期，将会抛出异常。ValidatingSessionManager异常捕获后，将调用onExpiration()作为内部事件，子类可以重写这个。
     * onExpiration本身的实现会出发onChange内部通知，然后notify所有sessionListener监听器。
     * 在DefaultSessionManager中重写了onChange().并在内部调用sessionDAO.update(session);-> cache.remove(key);
     * 该方法内部会根据验证来删除或更新session，TODO 本例中调用的是 ShiroRedisCache.remove(key);
     * TODO 注意：
     * 根据以上情形，onChange()->update(session) 调用在notify sessionListener之前。
     * 所以想在监听器中重新touch一下session以保证有效是不可行的。只会抛出异常。
     *
     * TODO deleteInvalidSessions=true 比较重要，
     * cacheManager本身除了通过 getCache()来获取Cache对象（这里我们设置成redis cache）。
     * 内部也维护者一个concurrentHashMap来缓存session。这样可以避免检查session频繁的从cache源获取cache导致性能急剧下降。
     * 将deleteInvalidSessions=true后，一方面在检查过期时会调用redis cache移除缓存，自身也需要将内部缓存的session删除。
     * @param redisCacheManager redis实现的 cachemanage
     * @return
     */
    @Bean("sessionManager")
    public DefaultWebSessionManager webSessionManager(SessionDAO sessionDAO,
                                                      ShiroRedisCacheManager redisCacheManager,
                                                      @Value("${server.servlet.session.timeout}") String timeout,
                                                      SessionProperties sessionProperties) {
        // Simple Cookie
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setHttpOnly(sessionProperties.isHttpOnly());
        simpleCookie.setName(sessionProperties.getCookieName());
        simpleCookie.setMaxAge(sessionProperties.getMaxAge());

        Duration timeoutDur = null;
        try {
           timeoutDur = DurationStyle.detectAndParse(timeout);
        }  catch (Exception e) {}

        MyShiroSessionManager webSessionManager = new MyShiroSessionManager();
        webSessionManager.setSessionIdCookieEnabled(sessionProperties.isCookieEnable());
        webSessionManager.setSessionDAO(sessionDAO);
        webSessionManager.setCacheManager(redisCacheManager);
        webSessionManager.setSessionIdCookie(simpleCookie);
        webSessionManager.setGlobalSessionTimeout(null == timeoutDur ? sessionProperties.getGlobalTimeout() : timeoutDur.getSeconds() * 1000);
        webSessionManager.setDeleteInvalidSessions(sessionProperties.isDeleteInvalidSessions());
        webSessionManager.setSessionValidationSchedulerEnabled(sessionProperties.isValidationSchedulerEnabled());
        webSessionManager.setSessionValidationInterval(sessionProperties.getValidationInterval());

        return webSessionManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionDAO shiroSessionDAO(ShiroRedisCacheManager redisCacheManager, SessionProperties sessionProperties) {
        // session dao
        EnterpriseCacheSessionDAO sessionDAO = new EnterpriseCacheSessionDAO();
        sessionDAO.setCacheManager(redisCacheManager);
        sessionDAO.setActiveSessionsCacheName(sessionProperties.getActiveSessionCacheName());
        sessionDAO.setSessionIdGenerator(new JavaUuidSessionIdGenerator());
        return sessionDAO;
    }
    // Ql232323

    /**
     * 使用shiro-web 默认的安全管理器。
     * 默认配置】
     * subjectFactory : 用于构建subject。默认为DefaultWebSubjectFactory(构建WebDelegatingSubject)
     * rememberMeManager : 用于确定使用什么方式记住自己。默认为 CookieRememberMeManager
     * 【注入】
     * sessionManager : 用于确定session的管理方式和生成方式。默认为 ServletContainerSessionManager(容器管理session)
     * 这里使用自己的session来进行管理
     * cacheManager : 缓存管理器，安全管理器可能将一些信息进行缓存（例如权限信息等），cacheManager为此提供缓存服务。
     * shiroDbRealm : 身份验证，自定义（是否使用单例，要看自己实现的方式）。
     * @return
     */
    @Bean(name = "securityManager")
    @DependsOn("userRealm")
    public DefaultWebSecurityManager securityManager(@Qualifier("userRealm") AuthorizingRealm userRealm,
                                                     ShiroRedisCacheManager shiroRedisCacheManager,
                                                     DefaultWebSessionManager sessionManager){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setRealm(userRealm);// 这里可以设置多个realm
        securityManager.setCacheManager(shiroRedisCacheManager);
        return securityManager;
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public AuthorizingRealm userRealm() {
//        throw new RuntimeException("请务必创建 AuthorizingRealm userRealm bean");
//    }

    /**
     * shiro的过滤器链
     * @param securityManager 安全管理器
     * @return
     */
    @Bean(name = "shiroFilter")
    @ConditionalOnMissingBean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager,
                                                         AuthProperties authProperties){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        shiroFilterFactoryBean.setLoginUrl(authProperties.getLoginUrl());
        shiroFilterFactoryBean.setSuccessUrl(authProperties.getSuccessUrl());
        shiroFilterFactoryBean.setUnauthorizedUrl(authProperties.getUnauthorizedUrl());


        Map<String, Filter> filters = new LinkedHashMap<>();
        shiroFilterFactoryBean.setFilters(filters);

        Map<String, String> filterChainDefinitionManager = new LinkedHashMap<>(authProperties.getFilterChainDefinitionMap());
        filterChainDefinitionManager.put("/**", "user");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionManager);

        return shiroFilterFactoryBean;
    }

    /**
     * DefaultAdvisorAutoProxyCreator，Spring的一个bean，由Advisor决定对哪些类的方法进行AOP代理。
     * 支持 Shiro对Controller的方法级AOP安全控制
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager defaultWebSecurityManager) {
        AuthorizationAttributeSourceAdvisor aasa = new AuthorizationAttributeSourceAdvisor();
        aasa.setSecurityManager(defaultWebSecurityManager);
        return aasa;
    }

    @Bean
    public FilterRegistrationBean<AbstractShiroFilter> sessionRepositoryFilterRegistration(ShiroFilterFactoryBean shiroFilterFactoryBean) throws Exception {
        FilterRegistrationBean<AbstractShiroFilter> registration = new FilterRegistrationBean<>((AbstractShiroFilter) shiroFilterFactoryBean.getObject());
        registration.setDispatcherTypes(DispatcherType.REQUEST,DispatcherType.ASYNC,DispatcherType.ERROR, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        registration.setOrder(0);
        return registration;
    }


//    @Bean
//    @Deprecated
//    public CrudControllerPermissionAdvisor basePermissionAdvisor(DefaultWebSecurityManager defaultWebSecurityManager) {
//        CrudControllerPermissionAdvisor advisor = new CrudControllerPermissionAdvisor();
//        advisor.setSecurityManager(defaultWebSecurityManager);
//        return advisor;
//    }

//    public static void main(String[] args) {
//        try {
//            String s = Hex.encodeHexString(
//                    Securitys.encode("MD5",
//                            "youandme".getBytes(),
//                            ByteSource.Util.bytes("f5615c95b78e50f544685a4ebdabe1ed").getBytes(),
//                            3)
//            );
//            System.out.println(s);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
}
