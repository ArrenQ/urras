package com.chuang.urras.web.office.controller;

import com.chuang.urras.crud.handlers.ValueGetter;
import com.chuang.urras.support.MapResult;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.BeanKit;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.toolskit.basic.tree.Node;
import com.chuang.urras.toolskit.basic.tree.NodeBuilder;
import com.chuang.urras.toolskit.third.javax.servlet.HttpKit;
import com.chuang.urras.web.office.SessionKeys;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.model.*;
import com.chuang.urras.web.office.service.single.IResourceService;
import com.chuang.urras.web.office.service.single.IRoleService;
import com.chuang.urras.web.office.service.single.IRunAsService;
import com.chuang.urras.web.office.service.single.IUserService;
import com.chuang.urras.web.office.shiro.WebUsernameAndPwdToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/auth")
@Api(tags = "认证模块")
public class AuthController extends BaseController {
    @javax.annotation.Resource private IUserService userService;
    @javax.annotation.Resource private IResourceService resourceService;
    @javax.annotation.Resource private IRunAsService runAsService;
    @javax.annotation.Resource private IRoleService roleService;
    @javax.annotation.Resource private UserAuthPolymerService userAuthPolymerService;
    @javax.annotation.Resource @Qualifier("operatorGetter") private ValueGetter<String> operatorGetter;



    /**
     * 登录逻辑
     *
     */
    @PostMapping(value = "/login")
    @ApiOperation("登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "captcha", value = "验证码", required = true, dataTypeClass = String.class)
    })
    public Result login(HttpServletRequest request,
                        HttpServletResponse response,
                        String username,
                        String password,
                        String captcha,
                        @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String ua,
                        @RequestHeader(name = HttpHeaders.REFERER, required = false) String referer) {
        try {

            verifyCaptcha(captcha, true);

            operatorGetter.set(username);
            //获取登录相关信息
            if (StringKit.isNotEmpty(referer)) {
                referer = request.getRequestURL().toString();
            }

            UsernamePasswordToken token =
                    new WebUsernameAndPwdToken(username, password, HttpKit.getIpAddress(request), ua, referer);
            SecurityUtils.getSubject().login(
                    token);

            User entity = userService.findByUsername(username)
                    .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "用户不存在"));

            entity.setLastLoginIp(HttpKit.getIpAddress(request));
            entity.setLastLoginTime(LocalDateTime.now());
            userService.updateById(entity);
            getSession().setAttribute(SessionKeys.LOGIN_USER, entity);

            changeLanguage(entity.getUseLanguage());

//            if (entity.getUsername().equalsIgnoreCase("admin")) {
//                simpMessagingTemplate.convertAndSend("/topic/notify", new Result(true, "notify.admin_login"));
//            }
            return Result.success();
        } catch(IncorrectCredentialsException e) {
            return Result.fail("密码错误");
        } catch(AuthenticationException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            //其他异常一律直接通知 前台登录错误
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error(e.getMessage());
            return Result.fail("登陆失败,系统异常");
        }
    }

    @GetMapping("/tree/all")
    @ApiOperation("获取所有权限的树结构")
    public Result tree() {
        Collection<Resource> resources = resourceService.list();

        List<Node<Resource>> nodeList = new NodeBuilder<Integer, Resource>().relation(userAuthPolymerService.relation).toNode(resources);

        return MapResult.success().data("tree", nodeList).toResult();
    }

    @GetMapping("/tree/available/all")
    @ApiOperation("获取所有有效权限的树结构")
    public Result availableAll() {
        Collection<Resource> resources = resourceService.findAvailable();

        List<Node<Resource>> nodeList = new NodeBuilder<Integer, Resource>().relation(userAuthPolymerService.relation).toNode(resources);

        return MapResult.success().data("tree", nodeList).toResult();
    }

    @GetMapping("/tree/nav")
    @ApiOperation("获取当前用户权限的树结构")
    public Result nav() {

        return MapResult.success().data("tree", userAuthPolymerService.getMenuTree(
                getCurrentPrincipals().stream().map(UserPrincipal::getId).collect(Collectors.toSet())
        )).toResult();
    }

    @GetMapping("/resources")
    @ApiOperation("通过roleId查询角色包含的所有权限")
    @ApiImplicitParam(name = "roleId", value = "角色id", required = true, dataTypeClass = Integer.class)
    public Result resources(Integer roleId) {
        return MapResult.success().data("resources", resourceService.findResourceByRoleId(roleId)).toResult();
    }

    @GetMapping("/resources/tree")
    @ApiOperation("通过roleId查询角色包含的所有权限树")
    @ApiImplicitParam(name = "roleId", value = "角色id", required = true, dataTypeClass = Integer.class)
    public Result resourcesTree(Integer roleId) {
        List<Resource> resources = resourceService.findResourceByRoleId(roleId);
        List<Node<Resource>> nodeList = new NodeBuilder<Integer, Resource>().relation(userAuthPolymerService.relation).toNode(resources);
        return MapResult.success().data("tree", nodeList).toResult();
    }

    @RequiresPermissions("role:update")
    @PostMapping("/resources/change")
    @ApiOperation("重置角色下的所有权限，要求用户有role:update权限的令牌")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "需要重置权限的角色id", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "ids", value = "角色重置后的所有权限id", required = true, dataTypeClass = Integer[].class)
    })
    public Result changeResources(Integer roleId, Integer[] ids) {
        Set<String> usernames = userAuthPolymerService.changeResources(roleId, ids);
        super.notifyRefreshInfo(usernames);
        return Result.success();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前登录用户的所有权限信息")
    public Result info() {
        Subject subject = SecurityUtils.getSubject();//当前使用的角色(不一定是登录用户)

        User loginUser = getLoginUser();  // 登录用户
        Set<UserPrincipal> principals = getCurrentPrincipals();// 当前使用的令牌用户

        boolean isRunAs = subject.isRunAs();

        Role role = roleService.getById(loginUser.getRoleId())
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "can not find role"));

        // 被当前登录用户授过权的用户
        List<UserPrincipal> myPrincipals = userAuthPolymerService.findPrincipalsByOwner(loginUser.getUsername());

        // 给当前登录用户授过权的所有用户
        List<UserPrincipal> awardedPrincipals = userAuthPolymerService.findAwardedPrincipals(loginUser.getUsername());
//        // 给当前用户授过权的用户中删除当前使用的。不用显示
//        fromUser.remove(principal.getUsername());
        // 我给别人的授权信息
        List<RunAs> runAs = runAsService.findByPrincipals(myPrincipals.stream().map(UserPrincipal::getId).collect(Collectors.toSet()));
        Map<Integer, Set<String>> runAsMap = runAs.stream().collect(
                groupingBy(RunAs::getPrincipalId, HashMap::new, mapping(RunAs::getToUsername, toSet()))
        );

        AuthInfoVO info = new AuthInfoVO();
        info.setPrincipals(BeanKit.convert(principals, HashSet::new, UserPrincipalVO::new));// 当前使用的令牌(令牌都是用户名，但不一定是当前登录的用户名)
        info.setUser(BeanKit.convertObj(loginUser, UserVO::new));// 当前登录用户
        info.setRunAs(isRunAs);// 是否使用的是别人的令牌
        info.setAuthMap(runAsMap);
        info.setRole(BeanKit.convertObj(role, RoleVO::new));
        info.setAwardedPrincipals(BeanKit.convert(awardedPrincipals, UserPrincipalVO::new));// 给当前登录用户授过权的用户
        info.setMyPrincipals(BeanKit.convert(myPrincipals, UserPrincipalVO::new));// 被当前登录用户 授予过权限 的所有用户

        return Result.success().data(info);


    }

    @PostMapping("/logout")
    @ApiOperation("登出当前用户")
    public Result logout() {
        Subject subject = SecurityUtils.getSubject();//当前使用的角色(不一定是登录用户)
        subject.logout();
        return Result.success();
    }

    @PostMapping(value = "/forceChangeUserPwd")
    @RequiresPermissions("auth:force-change-pwd")
    @ApiOperation("强制更新密码（不需要填入旧密码）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "password", value = "新密码", required = true, dataTypeClass = String.class)
    })
    public Result forceChangeUserPwd(String username, String password) {
        return Result.whether(userAuthPolymerService.changePassword(username, password));
    }

    @PostMapping("/changePwd")
    @ApiOperation("修改当前用户的密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "oldPwd", value = "旧密码", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "newPwd", value = "新密码", required = true, dataTypeClass = String.class)
    })
    public Result changePwd(String oldPwd, String newPwd) {
        boolean changed = userAuthPolymerService.changePasswordWithCheck(getLoginUser().getUsername(), oldPwd, newPwd);
        return Result.whether(changed);
    }

    @GetMapping("/unauthorized")
    public Result unauthorized() {
        return MapResult.fail("认证失败").data("unauthorized", true).toResult();
    }


}
