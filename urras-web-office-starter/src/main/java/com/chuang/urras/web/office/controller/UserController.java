package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.CRUD;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.BeanKit;
import com.chuang.urras.toolskit.third.javax.servlet.HttpKit;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.service.polymer.UserAuthPolymerService;
import com.chuang.urras.web.office.service.single.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Api(tags = "用户信息 CRUD")
public class UserController extends CrudController<User> {

    private UserAuthPolymerService userAuthPolymerService;
    @Autowired
    public UserController(UserAuthPolymerService userAuthPolymerService) {
        super("user");
        this.userAuthPolymerService = userAuthPolymerService;
    }

    @Override
    @PutMapping("/create")
    @RequiresPermissions("user:create")
    @ApiOperation("新增管理员账号")
    public Result create(@RequestBody User entity, HttpServletRequest request) {
        if(entity.getPassword() == null) {
            entity.setPassword("123456");
        }
        boolean success = userAuthPolymerService.register(entity);
        // 添加操作日志
        createOptLogs(success, HttpKit.getIpAddress(request), CRUD.CREATE, null, entity);
        return Result.success();
    }

    @Override
    @PostMapping("/update")
    @RequiresPermissions("user:update")
    @ApiOperation("更新管理员信息")
    public Result update(@RequestBody User vo, HttpServletRequest request) {
        vo.setRoleId(null);// 不能直接通过修改权限来更换role
        return super.update(vo, request);
    }

    @PostMapping("/changeRole")
    @RequiresPermissions("user:change-role")
    @ApiOperation("修改管理员角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "管理员账号", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "roleId", value = "角色ID", required = true, dataTypeClass = Integer.class)
    })
    public Result changeRole(String username, Integer roleId, HttpServletRequest request) {
        Optional<User> optional = ((IUserService)getService()).findByUsername(username);

        User entity = optional.orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "用户不存在"));
        User before = new User();
        BeanKit.copyProperties(entity, before);

        entity.setRoleId(roleId);
        boolean updated =  service.updateById(entity);
        createOptLogs(updated, HttpKit.getIpAddress(request), CRUD.UPDATE, before, entity);
        return Result.whether(updated);
    }
}
