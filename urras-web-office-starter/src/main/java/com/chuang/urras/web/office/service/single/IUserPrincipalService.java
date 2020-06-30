package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.UserPrincipal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * <p>
 * 用户令牌  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface IUserPrincipalService extends IService<UserPrincipal> {
    default Optional<UserPrincipal> findMainPrincipalByOwner(String username) {
        return Optional.ofNullable(lambdaQuery()
                    .eq(UserPrincipal::getOwner, username)
                    .eq(UserPrincipal::getMain, true)
                    .one());
    }

    default List<UserPrincipal> findPrincipalByOwners(Set<String> usernames) {
        if(usernames.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(UserPrincipal::getOwner, usernames).list();
    }
}
