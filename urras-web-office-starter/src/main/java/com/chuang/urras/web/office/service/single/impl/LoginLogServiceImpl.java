package com.chuang.urras.web.office.service.single.impl;

import com.chuang.urras.crud.service.ServiceImpl;
import com.chuang.urras.web.office.mapper.LoginLogMapper;
import com.chuang.urras.web.office.model.LoginLog;
import com.chuang.urras.web.office.service.single.ILoginLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员登陆日子  服务实现类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements ILoginLogService {

}
