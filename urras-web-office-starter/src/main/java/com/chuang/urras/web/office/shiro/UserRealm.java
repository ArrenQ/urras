package com.chuang.urras.web.office.shiro;

import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.web.office.model.Role;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.model.UserPrincipal;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.service.single.ILoginLogService;
import com.chuang.urras.web.office.service.single.IRoleService;
import com.chuang.urras.web.office.service.single.IUserPrincipalService;
import com.chuang.urras.web.office.service.single.IUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 权限管控,主要处理登录
 */
public class UserRealm extends AuthorizingRealm {
    private Logger logger = LoggerFactory.getLogger(UserRealm.class);

    @Resource
    private IUserService userService;

    @Resource
    private IRoleService roleService;

    @Resource
    private UserAuthPolymerService userAuthPolymerService;

    @Resource
    private ILoginLogService loginLogService;

    @Resource
    private IUserPrincipalService userPrincipalService;

    @Resource
    private MessageSource messageSource;

    /**
     * @param authcToken 认证Token
     * @return AuthenticationInfo 交给shiro认证(主要为了记录session等一些其他工作),实际上在方法内已经认证通过
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

        WebUsernameAndPwdToken token = (WebUsernameAndPwdToken) authcToken;

        Optional<User> optional;
        try {
            optional = userService.findByUsername(token.getUsername());
        } catch (Exception e) {
            logger.error("查找用户异常", e);
            throw new AuthenticationException(String.
                    format("username query error.loginName:%s,Ip:%s.message:\n%s", token.getUsername(), token.getHost(), e.getMessage()));
        }
        if (!optional.isPresent()) {
            throw new AuthenticationException(String.
                    format("username is not found.loginName:%s,Ip:%s.message:\n%s", token.getUsername(), token.getHost(), "webService return null"));
        }
        User userEntity = optional.get();
        if (userEntity.getState().isLocked()) {
            throw new AuthenticationException(getMessage("user.disable"));
        }

        token.setRealName(userEntity.getRealName());
        // 获取用户主令牌
        UserPrincipal mainPrincipal = userPrincipalService.findMainPrincipalByOwner(userEntity.getUsername())
                .orElseThrow(() -> new AuthenticationException(String.
                        format("user %s can not found a main principal, Please contact your system administrator for a fix ", token.getUsername())));

        return new SimpleAuthenticationInfo(mainPrincipal,
                userEntity.getPassword(), ByteSource.Util.bytes(Hex.decode(userEntity.getSalt())), getName());
    }


    /**
     * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
     * 使用两类权限,账户类型和币种类型
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();


        List<UserPrincipal> list = principals.asList();

        UserPrincipal mainPrincipal = null;

        Set<Integer> userPrincipalIds = new HashSet<>();

        for (UserPrincipal principal : list) {
            if(principal.getMain()) {
                mainPrincipal = principal;
            }
            userPrincipalIds.add(principal.getId());
        }
        if(null == mainPrincipal) {
            throw new SystemWarnException(Result.FAIL_CODE, "数据异常，无法找到主令牌");
        }

        User user = userService.findByUsername(mainPrincipal.getOwner())
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "数据异常，无法找到主令牌所属用户"));

        Role role = roleService.getById(user.getRoleId())
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "用户角色id无法找到角色"));
        info.addRole(role.getRole());

        info.addStringPermissions(userAuthPolymerService.findResources(
                userPrincipalIds).stream().map(com.chuang.urras.web.office.model.Resource::getPermission).collect(Collectors.toList())
        );

        return info;
    }

    /**
     * 该方法会直接调用super的实现去验证密码是否正确。<br/>
     * super的实现会根据内置的CredentialsMatcher对token的口令进行加密再和info中的口令匹配。<br/>
     * 如果匹配成功，继续执行。否则跑出异常，中断此次验证。<br/>
     * 这里我们直接使用super实现。写下来是为了说明整个验证过程中该拦截器的生命周期
     * 关于CredentialsMatcher的作用可以看 {@link UserRealm#onInit()}说明
     *
     * @param token 用户输入的令牌
     * @param info 令牌对应的身份信息
     * @throws AuthenticationException 中断验证，表示验证失败
     * @see UserRealm#onInit();
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        WebUsernameAndPwdToken upt = (WebUsernameAndPwdToken)token;

        String userAgent = upt.getUserAgent(),
                referer = upt.getReferer();//useragent表示浏览器

        super.assertCredentialsMatch(token, info);

        //记录登录日志
        loginLogService.addLoginLog(upt.getUsername(),
                upt.getRealName(),
                true,
                upt.getHost(),
                null,
                null,
                userAgent,
                referer);


    }
    protected String getMessage(String key, Object... args) {
        Locale locale = (Locale) getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
        return messageSource.getMessage(key, args, locale);
    }
    public Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

}
