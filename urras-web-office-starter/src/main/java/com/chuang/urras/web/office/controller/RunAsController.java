package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.web.office.model.UserPrincipal;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.service.single.IRunAsService;
import com.chuang.urras.web.office.service.single.IUserPrincipalService;
import com.chuang.urras.web.office.shiro.UserRealm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@Api(tags = "授权模块")
public class RunAsController extends BaseController {

    private final IRunAsService runAsService;


    private final UserRealm userRealm;

    private final UserAuthPolymerService userAuthPolymerService;

    private final IUserPrincipalService userPrincipalService;

    @Autowired
    public RunAsController(IRunAsService runAsService,
                           UserRealm userRealm,
                           UserAuthPolymerService userAuthPolymerService,
                           IUserPrincipalService userPrincipalService) {
        this.runAsService = runAsService;
        this.userRealm = userRealm;
        this.userAuthPolymerService = userAuthPolymerService;
        this.userPrincipalService = userPrincipalService;
    }

    /**
     * 授权
     * @param userPrincipalId 当前用户令牌id
     * @param toUsername 被授权人账号
     */
    @PostMapping("/award/principal")
    @ApiOperation("给指定用户进行授权，授权令牌为当前登录用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userPrincipalId", value = "令牌Id", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "toUsername", value = "接受授权的用户名", required = true, dataTypeClass = String.class)
    })
    public Result awardPrincipal(Integer userPrincipalId, String toUsername) {
        String fromUser = getLoginUser().getUsername();//授权只能授权登录用户，而不是当前使用的 principal。
//        Set<Long> set = Arrays.stream(userPrincipalId).collect(Collectors.toSet());
        boolean success = userAuthPolymerService.awardPrincipal(fromUser, toUsername, userPrincipalId);
        return Result.whether(success);
    }

    /**
     * 收回令牌
     * @param principalId 用户令牌id
     */
    @PostMapping("/takeback/principal")
    @ApiOperation("收回令牌")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "beRetrievedUser", value = "被收回令牌的用户", required = true, dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "principalId", value = "令牌Id", required = true, dataTypeClass = Long.class)
    })
    public Result takeBackPrincipal(String beRetrievedUser, Integer principalId) {
        String from = getLoginUser().getUsername();
        boolean success = userAuthPolymerService.takeBackPrincipal(from, beRetrievedUser, principalId);
        if(success) {
            super.notifyRefreshInfo(Collections.singleton(beRetrievedUser));
        }
        return Result.success();
    }

    /**
     * 归还令牌
     * @param principalId 令牌
     */
    @PostMapping("/return/principal")
    @ApiOperation("归还当前登录用户所拥有的授权")
    @ApiImplicitParam(name = "principalId", value = "归还的令牌", required = true, dataTypeClass = Long.class)
    public Result returnPrincipal(Integer principalId) {
        Subject subject = SecurityUtils.getSubject();
        while (subject.isRunAs()) {
            subject.releaseRunAs();
        }

        String giveBackUser = getLoginUser().getUsername();
        runAsService.giveBack(giveBackUser, principalId);
        return Result.success();
    }

    /**
     * 返回上一次使用的权限
     */
    @PostMapping("/previous/principal")
    @ApiOperation("返回上一个使用的授权")
    public Result previousPrincipal() {
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isRunAs()) {
            return Result.fail("您没有使用别人的资质");
        }
//        UserEntity beforePrincipal = (UserEntity) subject.getPreviousPrincipals().getPrimaryPrincipal();
//        if(beforePrincipal.getStatus().isLocked()) {
//            return Result.fail("该资质已经被锁，无法使用");
//        }
        subject.releaseRunAs();
        return Result.success();
    }

    /**
     * 不再使用别人给我的令牌
     * @param principalId 令牌ID
     */
    @PostMapping("/nonuse/principal")
    @ApiOperation("使用指定的令牌")
    @ApiImplicitParam(name = "principalId", value = "不再使用的令牌", required = true, dataTypeClass = Integer.class)
    public Result noUsePrincipal(Integer principalId) {
        Subject subject = SecurityUtils.getSubject();

        Set<UserPrincipal> userPrincipals = new HashSet<>(getCurrentPrincipals());
        for(UserPrincipal entity : userPrincipals) {
            if (entity.getId().equals(principalId)) {
                userPrincipals.remove(entity);
                subject.runAs(new SimplePrincipalCollection(userPrincipals, userRealm.getName()));
                break;
            }
        }
        return Result.success();
    }

    /**
     * 使用别人给我的令牌
     * @param principalId 令牌ID
     */
    @PostMapping("/use/principal")
    @ApiOperation("使用指定的令牌")
    @ApiImplicitParam(name = "principalId", value = "需要使用的令牌", required = true, dataTypeClass = Integer.class)
    public Result usePrincipal(Integer principalId) {
        String to = getLoginUser().getUsername();

        if(!runAsService.exists(to, principalId)) {
            throw new SystemWarnException(Result.FAIL_CODE, "您没有获得这个令牌");
        }

        Subject subject = SecurityUtils.getSubject();

        Set<UserPrincipal> userPrincipals = new HashSet<>(getCurrentPrincipals());



        for(UserPrincipal entity : userPrincipals) {
            if (entity.getId().equals(principalId)) {
                throw new SystemWarnException(Result.FAIL_CODE, "您早已使用这个令牌。");
            }
        }
        UserPrincipal principal= userPrincipalService.getById(principalId)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "该令牌不存在"));

        userPrincipals.add(principal);
        subject.runAs(new SimplePrincipalCollection(userPrincipals, userRealm.getName()));
        return Result.success();
    }


}
