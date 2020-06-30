package com.chuang.urras.web.office.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chuang.urras.web.office.model.Resource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ResourceMapper extends BaseMapper<Resource> {

    @Delete("delete from t_role_resource where role_id=#{roleId}")
    int deleteRoleResource(@Param("roleId") Integer roleId);

    @Insert("insert into t_role_resource (role_id, resource_id) values (#{roleId}, #{resourceId})")
    int saveRoleResource(@Param("roleId") Integer roleId, @Param("resourceId") Integer resourceId);

    @Select("SELECT p.id, p.name, p.i18n, p.type, p.url, p.parent_id as parentId, p.path as path, p.permission, p.available, p.icon, p.description " +
            "FROM t_resource p " +
            "LEFT JOIN t_role_resource rp " +
            "ON p.id=rp.resource_id " +
            "WHERE rp.role_id=#{roleId} " +
            "AND p.available=1")
    List<Resource> selectRoleResource(@Param("roleId") Integer roleId);

    @Insert("<script>" +
            "insert into t_role_resource (role_id, resource_id) values " +
            "<foreach item='item' collection='resourceIds' separator=','>" +
            "  (#{roleId}, #{item})  " +
            "</foreach>" +
            "</script>")
    int saveRoleResources(@Param("roleId") Integer roleId, @Param("resourceIds") Integer[] resourceIds);
}