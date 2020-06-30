package com.chuang.urras.web.office.service.polymer;


import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.tree.IRelation;
import com.chuang.urras.toolskit.basic.tree.Node;
import com.chuang.urras.web.office.model.Resource;
import com.chuang.urras.web.office.model.Role;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.model.UserPrincipal;

import java.util.List;
import java.util.Set;

public interface UserAuthPolymerService {
    /**
     * ResourceEntity 的关系接口
     */
    IRelation<Integer, Resource> relation = new IRelation<Integer, Resource>() {
        @Override
        public Integer parentID(Resource o) {
            if(o.getParentId() == 0 || o.getId().equals(o.getParentId())) {
                return null;
            }
            return o.getParentId();
        }

        @Override
        public Integer myID(Resource o) {
            return o.getId();
        }
    };

    /**
     * 修改密码
     */
    boolean changePassword(String username, String newPwd) throws SystemWarnException;

    boolean changePasswordWithCheck(String username, String oldPwd, String newPwd);


    /**
     * 获取用户权限菜单树
     * @param principals 令牌集
     */
    List<Node<Resource>> getMenuTree(Set<Integer> principals);

    /**
     * 注册
     */
    boolean register(User entity);

    boolean updateRoleWithResource(Role role, List<Integer> resourceIds);

//    Set<String> findResources(Integer roleId);

    boolean awardPrincipal(String fromUsername, String toUsername, Integer userPrincipalId);

    /**
     * 批量授予令牌，如果userPrincipalIds 中的存在不属于 fromUsername的令牌，那么这些令牌将会过滤。
     * @param fromUsername 授予者
     * @param toUsername 被授予者
     * @param userPrincipalIds 令牌集合，如果集合中存在不属于 授予者 的令牌，那么这些令牌将被过滤
     * @return 是否成功
     */
    boolean awardPrincipal(String fromUsername, String toUsername, Set<Integer> userPrincipalIds);

    boolean takeBackPrincipal(String from, String to, Integer principalId);

    List<Resource> findResources(Set<Integer> userPrincipalIds);

    List<Resource> findResourcesByPrincipal(String owner, Integer principalId);

    /**
     * 查找给 toUsername 授权的令牌对象，如果令牌id实际没有授权，则过滤掉这些令牌。
     * @param awardedUser 被授权用户
     * @param principalIdSet 令牌id集
     * @return 用户令牌对象集
     */
    List<UserPrincipal> findAwardedPrincipals(String awardedUser, Set<Integer> principalIdSet);

    List<UserPrincipal> findAwardedPrincipals(String awardedUser);

    List<UserPrincipal> findPrincipalsByOwner(String owner);

    /**
     * 铸造令牌
     * @param owner 铸造者
     * @param principalName 令牌名
     * @param resourceIds 资源名
     * @return 令牌
     */
    UserPrincipal makeUserPrincipal(String owner, String principalName, Integer[] resourceIds);

    /**
     * 令牌重新赋权，该方法会验证 principalId 令牌是否属于owner
     * @param owner 令牌所属
     * @param principalId 令牌id
     * @param resourceIds 令牌重新赋权的资源
     */
    void reAuthPrincipalResources(String owner, Integer principalId, Integer[] resourceIds);

    /**
     * 修改角色权限
     * @param roleId 角色id
     * @param ids 权限id
     * @return 受影响的用户
     */
    Set<String> changeResources(Integer roleId, Integer[] ids);
}
