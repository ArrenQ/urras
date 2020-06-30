package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.UserPrincipalResource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户令牌资源  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface IUserPrincipalResourceService extends IService<UserPrincipalResource> {

    default Set<Integer> findResourceIds(Set<Integer> userPrincipalIds) {
        if(userPrincipalIds.isEmpty()) {
            return Collections.emptySet();
        }
        return lambdaQuery().in(UserPrincipalResource::getUserPrincipalId, userPrincipalIds)
                .list()
                .stream()
                .map(UserPrincipalResource::getResourceId)
                .collect(Collectors.toSet());
    }

    default boolean deleteByPrincipalId(Integer userPrincipalId) {
        return lambdaUpdate().eq(UserPrincipalResource::getUserPrincipalId, userPrincipalId).remove();
    }

    default boolean deleteByPrincipalIds(Set<Integer> userPrincipalId) {
        if(userPrincipalId.isEmpty()) {
            return true;
        }
        return lambdaUpdate().in(UserPrincipalResource::getUserPrincipalId, userPrincipalId).remove();
    }

    default boolean deleteByPrincipalIdsAndResourceIds(Set<Integer> userPrincipalId, Set<Integer> resourceIds) {
        if(resourceIds.isEmpty() || userPrincipalId.isEmpty()) {
            return true;
        }
        return lambdaUpdate()
                .in(UserPrincipalResource::getUserPrincipalId, userPrincipalId)
                .in(UserPrincipalResource::getResourceId, resourceIds)
                .remove();
    }
}
