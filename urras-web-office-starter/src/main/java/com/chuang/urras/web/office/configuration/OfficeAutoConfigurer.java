package com.chuang.urras.web.office.configuration;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.chuang.urras.crud.RowQueryConverter;
import com.chuang.urras.crud.handlers.AutoTimeHandler;
import com.chuang.urras.crud.handlers.ThreadLocalValueGetter;
import com.chuang.urras.crud.handlers.ValueGetter;
import com.chuang.urras.web.office.PrincipalExpireInterceptor;
import com.chuang.urras.web.office.shiro.UserRealm;
import com.chuang.urras.web.shiro.configuration.ShiroAutoConfiguration;
import com.chuang.urras.web.shiro.properties.RealmProperties;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan("com.chuang.urras.web.office")
@Import({ShiroAutoConfiguration.class, OperatorInterceptor.class})
public class OfficeAutoConfigurer implements WebMvcConfigurer {
//    @Override
//    public Validator getValidator() {
//        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
//        localValidatorFactoryBean.setValidationMessageSource((MessageSource) beanFactory.getBean("messageSource"));
//        localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
//        return localValidatorFactoryBean;
//    }

//    @Bean
//    @ConditionalOnMissingBean
//    @DependsOn("sysPropertiesService")
//    public SystemSettingPolicy systemSettingPolicy(SysPropertiesService sysPropertiesService) {
//        return new CachedDBSystemSettingPolicy(sysPropertiesService);
//    }

    @Bean("operatorGetter")
    @ConditionalOnMissingBean
    public ValueGetter<String> operatorGetter() {
        return new ThreadLocalValueGetter<>();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler autoTimeHandler(@Qualifier("operatorGetter") ValueGetter<String> operatorGetter) {
        return new AutoTimeHandler(operatorGetter);
    }

    /**
     *
     * @param hashedCredentialsMatcher 校验器
     */
    @Bean("userRealm")
    @DependsOn("messageSource")
    @ConditionalOnMissingBean
    public UserRealm userRealm(HashedCredentialsMatcher hashedCredentialsMatcher,
                               RealmProperties realmProperties) {
        UserRealm userRealm = new UserRealm();
        userRealm.setCachingEnabled(realmProperties.isCacheEnabled());
        userRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        userRealm.setAuthenticationCachingEnabled(realmProperties.isAuthenticationCacheEnabled());
        userRealm.setAuthenticationCacheName(realmProperties.getAuthenticationCacheName());
        userRealm.setAuthorizationCachingEnabled(realmProperties.isAuthorizationCacheEnabled());
        userRealm.setAuthorizationCacheName(realmProperties.getAuthorizationCacheName());
        return userRealm;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 国际化操作拦截器 如果采用基于（请求/Session/Cookie）则必需配置
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new PrincipalExpireInterceptor());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new RowQueryConverter());
    }

    /**
     * 国际化配置，spring-boot似乎只需要配置这个即可，且name一定要是 localeResolver
     */
    @Bean(name="localeResolver")
    @ConditionalOnMissingBean
    public LocaleResolver localeResolverBean() {
        return new SessionLocaleResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        List<ISqlParser> sqlParserList = new ArrayList<>();
        // 攻击 SQL 阻断解析器、加入解析链
//        sqlParserList.add(new BlockAttackSqlParser());
        paginationInterceptor.setSqlParserList(sqlParserList);
        return paginationInterceptor;
    }
}
