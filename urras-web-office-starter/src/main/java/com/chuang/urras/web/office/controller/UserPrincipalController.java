package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.CRUD;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.BeanKit;
import com.chuang.urras.toolskit.third.javax.servlet.HttpKit;
import com.chuang.urras.web.office.model.RunAs;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.model.UserPrincipal;
import com.chuang.urras.web.office.model.UserPrincipalVO;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.service.single.IRunAsService;
import com.chuang.urras.web.office.service.single.IUserPrincipalResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/principal")
@Api(tags = "用户令牌 CRUD")
public class UserPrincipalController extends CrudController<UserPrincipal> {

    private final UserAuthPolymerService userAuthPolymerService;
    private final IUserPrincipalResourceService userPrincipalResourceService;
    private final IRunAsService runAsService;
    @Autowired
    public UserPrincipalController(IUserPrincipalResourceService userPrincipalResourceService,
                                   UserAuthPolymerService userAuthPolymerService,
                                   IRunAsService runAsService) {
        super("user-principal");
        this.userAuthPolymerService = userAuthPolymerService;
        this.userPrincipalResourceService = userPrincipalResourceService;
        this.runAsService = runAsService;
    }


    @PostMapping("/personal/make")
    @ApiOperation("个人铸造令牌(与管理员铸造令牌的权限不同)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "principalName", value = "令牌名称", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "resourceIds", value = "资源id", required = true, dataTypeClass = Integer[].class)
    })
    @RequiresPermissions("user-principal-personal:make")
    public Result<UserPrincipalVO> makePrincipal(String principalName, Integer[] resourceIds) {
        UserPrincipal principal = userAuthPolymerService.makeUserPrincipal(getLoginUser().getUsername(), principalName, resourceIds);
        return Result.success(BeanKit.convertObj(principal, UserPrincipalVO::new));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("根据id删除一条记录")
    @RequiresPermissions("user-principal:delete")
    public Result deleteByKey(@PathVariable("id") String id, HttpServletRequest request) {
        Optional<UserPrincipal> before = service.getById(id);
        boolean deleted = service.removeById(id);

        userPrincipalResourceService.deleteByPrincipalId(Integer.parseInt(id));
        // 添加操作日志
        createOptLogs(deleted, HttpKit.getIpAddress(request), CRUD.DELETE, before.orElse(null), null);

        List<RunAs> runs = runAsService.findByPrincipal(Integer.parseInt(id));
        if(!runs.isEmpty()) {
            Set<String> affectedUsers = runs.stream().map(RunAs::getToUsername).collect(Collectors.toSet());
            runAsService.deleteByPrincipalId(Integer.parseInt(id));
            notifyRefreshInfo(affectedUsers);
        }
        return Result.whether(deleted);
    }

    @DeleteMapping("/personal/delete/{id}")
    @ApiOperation("根据id删除一条记录")
//    @RequiresPermissions("user-principal-personal:delete")
    public Result userDelete(@PathVariable("id") Integer id) {
        User user = getLoginUser();
        UserPrincipal principal = service.getById(id)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "令牌不存在"));
        if(!user.getUsername().equals(principal.getOwner())) {
            throw new SystemWarnException(Result.FAIL_CODE, "令牌不属于您");
        }

        if(principal.getMain()) {
            throw new SystemWarnException(Result.FAIL_CODE, "主令牌不能删除");
        }

        service.removeById(id);
        userPrincipalResourceService.deleteByPrincipalId(id);

        List<RunAs> runs = runAsService.findByPrincipal(id);
        if(!runs.isEmpty()) {
            Set<String> affectedUsers = runs.stream().map(RunAs::getToUsername).collect(Collectors.toSet());
            runAsService.deleteByPrincipalId(id);
            notifyRefreshInfo(affectedUsers);
        }

        return Result.success();
    }

}
