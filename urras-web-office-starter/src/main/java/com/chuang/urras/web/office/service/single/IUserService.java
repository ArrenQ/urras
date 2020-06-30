package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserService extends IService<User> {
    default List<User> findByRoleId(Integer roleId) {
        return lambdaQuery().eq(User::getRoleId, roleId).list();
    }

    default Optional<User> findByUsername(String username) {
        return Optional.ofNullable(lambdaQuery().eq(User::getUsername, username).one());
    }

}
