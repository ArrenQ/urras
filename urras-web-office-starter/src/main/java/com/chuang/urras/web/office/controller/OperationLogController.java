package com.chuang.urras.web.office.controller;

import com.chuang.urras.web.office.model.OperationLog;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log/operation")
@Api(tags = "操作日志 CRUD")
public class OperationLogController extends CrudController<OperationLog> {
    public OperationLogController() {
        super("operation-log");
    }
}
