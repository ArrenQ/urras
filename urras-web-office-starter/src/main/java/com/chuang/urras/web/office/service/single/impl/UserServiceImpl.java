package com.chuang.urras.web.office.service.single.impl;

import com.chuang.urras.crud.service.ServiceImpl;
import com.chuang.urras.web.office.mapper.UserMapper;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.service.single.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员  服务实现类
 * </p>
 *
 * @author ath
 * @since 2020-02-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
