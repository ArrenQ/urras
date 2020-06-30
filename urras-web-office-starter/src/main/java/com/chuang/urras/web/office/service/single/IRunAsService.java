package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.RunAs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * runAs  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface IRunAsService extends IService<RunAs> {
    default boolean exists(String toUsername, Integer userPrincipalId) {
        return findOne(toUsername, userPrincipalId).isPresent();
    }


    default boolean giveBack(String returnUsername, Integer principalId) {
        return lambdaUpdate()
                .eq(RunAs::getToUsername, returnUsername)
                .eq(RunAs::getPrincipalId, principalId)
                .remove();
    }

    default Optional<RunAs> findOne(String toUsername, Integer userPrincipalId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(RunAs::getPrincipalId, userPrincipalId)
                .eq(RunAs::getToUsername, toUsername)
                .one());
    }

    default List<RunAs> selectByToUsername(String toUsername) {
        return lambdaQuery().eq(RunAs::getToUsername, toUsername).list();
    }

    default List<RunAs> findByPrincipals(Set<Integer> userPrincipalIds) {
        if(userPrincipalIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RunAs::getPrincipalId, userPrincipalIds).list();
    }

    default List<RunAs> findByPrincipal(Integer principalId) {
        return lambdaQuery().eq(RunAs::getPrincipalId, principalId).list();
    }

    default boolean deleteByPrincipalId(Integer principalId) {
        return lambdaUpdate().eq(RunAs::getPrincipalId, principalId).remove();
    }
}
