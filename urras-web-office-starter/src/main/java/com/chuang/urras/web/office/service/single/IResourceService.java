package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.web.office.model.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 资源表  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface IResourceService extends IService<Resource> {

    int deleteRoleResource(Integer roleId);

    int saveRoleResource(Integer roleId, Integer resourceId);

    List<Resource> findResourceByRoleId(Integer roleId);

    boolean changeResources(Integer roleId, Integer[] resources);

    default List<Resource> findAvailable() {
        return lambdaQuery().eq(Resource::getAvailable, true).list();
    }

    default List<Resource> findResources(Set<Integer> resourceIds) {
        if(resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(Resource::getId, resourceIds).list();
    }
}
