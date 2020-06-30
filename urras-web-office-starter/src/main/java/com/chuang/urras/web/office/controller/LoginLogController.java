package com.chuang.urras.web.office.controller;

import com.chuang.urras.web.office.model.LoginLog;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log/login")
@Api(tags = "登录日志 CRUD")
public class LoginLogController extends CrudController<LoginLog> {

    public LoginLogController() {
        super("user-login-log");
    }
}
