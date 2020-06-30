package com.chuang.urras.web.office.service.single.impl;

import com.chuang.urras.crud.service.ServiceImpl;
import com.chuang.urras.web.office.mapper.OperationLogMapper;
import com.chuang.urras.web.office.model.OperationLog;
import com.chuang.urras.web.office.service.single.IOperationLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作时间  服务实现类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

}
