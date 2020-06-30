package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.Role;

import java.util.List;

/**
 * <p>
 * 角色  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface IRoleService extends IService<Role> {
    default List<Role> findAvailable() {
        return lambdaQuery().eq(Role::getAvailable, true).list();
    }
}
