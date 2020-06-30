package com.chuang.urras.web.office.controller;

import com.chuang.urras.web.office.model.Resource;
import com.chuang.urras.web.office.service.single.IResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by ath on 2016/3/7.
 */
@RestController
@RequestMapping("/resource")
@Api(tags = "权限信息 CRUD")
public class ResourceController extends CrudController<Resource> {

    @Autowired
    public ResourceController() {
        super("resource");
    }


    @RequestMapping("/query/available")
    @RequiresPermissions("resource:view")
    @ApiOperation("获取所有有效权限")
    public List<Resource> available() {
        return ((IResourceService)service).findAvailable();
    }

}
