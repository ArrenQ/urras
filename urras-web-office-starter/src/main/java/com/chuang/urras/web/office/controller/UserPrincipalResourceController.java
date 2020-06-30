package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.Result;
import com.chuang.urras.web.office.model.Resource;
import com.chuang.urras.web.office.model.RunAs;
import com.chuang.urras.web.office.model.UserPrincipalResource;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.service.single.IRunAsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/principal/resource")
@Api(tags = "用户令牌权限 CRUD")
public class UserPrincipalResourceController extends CrudController<UserPrincipalResource> {
    private final UserAuthPolymerService userAuthPolymerService;

    private final IRunAsService runAsService;

    public UserPrincipalResourceController(UserAuthPolymerService userAuthPolymerService, IRunAsService runAsService) {
        super("user-principal-resource");
        this.userAuthPolymerService = userAuthPolymerService;
        this.runAsService = runAsService;
    }

    @PostMapping("/personal/reAuth")
    @ApiOperation("给自己的令牌重新赋权，当令牌被重新赋权时，所有被授予了该令牌的用户将被强制登录。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalId", value = "令牌id", required = true, dataTypeClass = Long.class),
            @ApiImplicitParam(name = "resourceIds", value = "资源id", required = true, dataTypeClass = Integer[].class)
    })
//    @RequiresPermissions("user-principal-resource-personal:re-auth")
    public Result reAuthPrincipal(Integer principalId, Integer[] resourceIds) {
        userAuthPolymerService.reAuthPrincipalResources(getLoginUser().getUsername(), principalId, resourceIds);

        List<String> toUsers = runAsService.findByPrincipal(principalId).stream().map(RunAs::getToUsername).collect(Collectors.toList());
        super.notifyRefreshInfo(toUsers);
        return Result.success();
    }

    @GetMapping("/personal/query")
    @ApiOperation("查询自己令牌的权限集合。")
    @ApiImplicitParam(name = "principalId", value = "令牌id", required = true, dataTypeClass = Integer.class)
    public Result<List<Resource>> findPrincipalResources(Integer principalId) {
        return Result.success(userAuthPolymerService.findResourcesByPrincipal(getLoginUser().getUsername(), principalId));
    }
}
