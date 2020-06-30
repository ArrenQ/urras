package com.chuang.urras.web.office.service.single.impl;

import com.chuang.urras.crud.service.ServiceImpl;
import com.chuang.urras.web.office.mapper.ResourceMapper;
import com.chuang.urras.web.office.model.Resource;
import com.chuang.urras.web.office.service.single.IResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 资源表  服务实现类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceService {
    @Override
    public int deleteRoleResource(Integer roleId) {
        return baseMapper.deleteRoleResource(roleId);
    }

    @Override
    public int saveRoleResource(Integer roleId, Integer resourceId) {
        return baseMapper.saveRoleResource(roleId, resourceId);
    }

    @Override
    public List<Resource> findResourceByRoleId(Integer roleId) {
        return baseMapper.selectRoleResource(roleId);
    }

    @Override
    public boolean changeResources(Integer roleId, Integer[] resources) {
        deleteRoleResource(roleId);
        return baseMapper.saveRoleResources(roleId, resources) > 0;
    }
}
