package com.chuang.urras.web.office.controller;

import com.chuang.urras.web.office.model.Role;
import com.chuang.urras.web.office.service.single.IRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/role")
@Api(tags = "角色信息 CRUD")
public class RoleController extends CrudController<Role> {


    @Autowired
    public RoleController() {
        super("role");
    }


    @RequestMapping("/query/available")
    @RequiresPermissions("role:view")
    @ApiOperation("获取所有有效角色")
    public List<Role> available() {
        return ((IRoleService)service).findAvailable();
    }

}
