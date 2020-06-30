package com.chuang.urras.web.office.service.polymer;


import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.Language;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.CollectionKit;
import com.chuang.urras.toolskit.basic.HashKit;
import com.chuang.urras.toolskit.basic.HexKit;
import com.chuang.urras.toolskit.basic.tree.Node;
import com.chuang.urras.toolskit.basic.tree.NodeBuilder;
import com.chuang.urras.web.office.model.*;
import com.chuang.urras.web.office.service.single.*;
import com.chuang.urras.web.shiro.properties.HashedCredentialProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("userAuthPolymerService")
public class UserAuthPolymerServiceImpl implements UserAuthPolymerService {

    private final IUserService userService;

    private final IRoleService roleService;

    private final IResourceService resourceService;

    private final IRunAsService runAsService;

    private final HashedCredentialProperties hashedCredentialProperties;

    private final IUserPrincipalService userPrincipalService;

    private final IUserPrincipalResourceService userPrincipalResourceService;

    @Autowired
    public UserAuthPolymerServiceImpl(HashedCredentialProperties hashedCredentialProperties,
                                      IUserService userService,
                                      IRoleService roleService,
                                      IResourceService resourceService,
                                      IRunAsService runAsService,
                                      IUserPrincipalService userPrincipalService,
                                      IUserPrincipalResourceService userPrincipalResourceService) {
        this.hashedCredentialProperties = hashedCredentialProperties;
        this.userService = userService;
        this.roleService = roleService;
        this.resourceService = resourceService;
        this.runAsService = runAsService;
        this.userPrincipalService = userPrincipalService;
        this.userPrincipalResourceService = userPrincipalResourceService;
    }

    @Override
    public boolean changePassword(String username, String newPwd) throws SystemWarnException {
        Optional<User> optional = userService.findByUsername(username);

        User entity = optional.orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "用户不存在"));
        byte [] salt = HashKit.genSalt(hashedCredentialProperties.getSaltLen());
        String newPwdHash = HashKit.encodeHex(hashedCredentialProperties.getAlgorithm(), newPwd.getBytes(), salt, hashedCredentialProperties.getIterations());

        entity.setSalt(HexKit.encodeHexStr(salt));
        entity.setPassword(newPwdHash);
        return userService.updateById(entity);
    }

    @Override
    public boolean changePasswordWithCheck(String username, String oldPwd, String newPwd) {
        Optional<User> optional = userService.findByUsername(username);

        User entity = optional.orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "用户不存在"));

        String oldPwdHash = HashKit.encodeHex(hashedCredentialProperties.getAlgorithm(), oldPwd.getBytes(), HexKit.decodeHex(entity.getSalt().toCharArray()), hashedCredentialProperties.getIterations());
        if(!oldPwdHash.equalsIgnoreCase(entity.getPassword())) {
            throw new SystemWarnException(Result.FAIL_CODE, "原密码错误");
        }

        byte [] salt = HashKit.genSalt(hashedCredentialProperties.getSaltLen());
        String newPwdHash = HashKit.encodeHex(hashedCredentialProperties.getAlgorithm(), newPwd.getBytes(), salt, hashedCredentialProperties.getIterations());

        entity.setSalt(HexKit.encodeHexStr(salt));
        entity.setPassword(newPwdHash);
        return userService.updateById(entity);
    }

    @Override
    public List<Resource> findResources(Set<Integer> userPrincipalIds) {
        if(userPrincipalIds.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Integer> resourceIds = userPrincipalResourceService.findResourceIds(userPrincipalIds);
        if(resourceIds.isEmpty()) {
            return new ArrayList<>();
        }
        return resourceService.findResources(resourceIds);
    }

    @Override
    public List<Resource> findResourcesByPrincipal(String owner, Integer principalId) {
        UserPrincipal principal = userPrincipalService.getById(principalId)
            .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "令牌不存在"));
        if(!owner.equals(principal.getOwner())) {
            throw new SystemWarnException(Result.FAIL_CODE, "令牌不属于" + owner);
        }

        return findResources(Collections.singleton(principalId));
    }

    @Override
    public List<UserPrincipal> findAwardedPrincipals(String awardedUser, Set<Integer> principalIdSet) {
        if(principalIdSet.isEmpty()) {
            return new ArrayList<>();
        }
        // 过滤没有授权的id
        Set<Integer> filterIds = runAsService.lambdaQuery()
                .in(RunAs::getPrincipalId, principalIdSet)
                .eq(RunAs::getToUsername, awardedUser)
                .list()
                .stream()
                .map(RunAs::getId)
                .collect(Collectors.toSet());


        if(filterIds.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userPrincipalService.listByIds(filterIds));
    }

    @Override
    public List<UserPrincipal> findAwardedPrincipals(String awardedUser) {
        // 过滤没有授权的id
        Set<Integer> filterIds = runAsService.selectByToUsername(awardedUser).stream()
                .map(RunAs::getPrincipalId).collect(Collectors.toSet());

        if(filterIds.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userPrincipalService.listByIds(filterIds));

    }

    @Override
    public List<UserPrincipal> findPrincipalsByOwner(String owner) {
        return userPrincipalService.lambdaQuery().eq(UserPrincipal::getOwner, owner).list();
    }

    @Override
    public UserPrincipal makeUserPrincipal(String owner, String principalName, Integer[] resourceIds) {


        User user = getUserAndCheckStatus(owner);


        Set<Integer> roleResourceIds = resourceService.findResourceByRoleId(user.getRoleId())
                .stream().map(Resource::getId).collect(Collectors.toSet());

        List<Integer> resourceIdList = Arrays.asList(resourceIds);
        List<Integer> subtract = CollectionKit.subtract(resourceIdList, roleResourceIds, ArrayList::new);
        if(!subtract.isEmpty()) {
            throw new SystemWarnException(Result.FAIL_CODE, "提交的权限中包含了您的身份中不存在的权限，请尝试重新选择并提交");
        }

        UserPrincipal entity = new UserPrincipal()
                .setOwner(owner)
                .setPrincipalName(principalName)
                .setMain(false);
        userPrincipalService.save(entity);

        List<UserPrincipalResource> prs = resourceIdList.stream().map(resourceId -> {
            UserPrincipalResource resourceEntity = new UserPrincipalResource();
            resourceEntity.setUserPrincipalId(entity.getId());
            resourceEntity.setResourceId(resourceId);
            return resourceEntity;
        }).collect(Collectors.toList());

        userPrincipalResourceService.saveBatch(prs);

        return entity;
    }

    @Override
    public void reAuthPrincipalResources(String owner, Integer principalId, Integer[] resourceIds) {
        if(resourceIds.length == 0) {
            return;
        }
        UserPrincipal principal = userPrincipalService.getById(principalId)
            .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "令牌不存在"));

        if(!principal.getOwner().equals(owner)) {
            throw new SystemWarnException(Result.FAIL_CODE, "该令牌不属于您，无法给它进行赋权");
        }

        User user = getUserAndCheckStatus(owner);

        Set<Integer> roleResourceIds = resourceService.findResourceByRoleId(user.getRoleId())
                .stream().map(Resource::getId).collect(Collectors.toSet());

        List<Integer> resourceIdList = Arrays.asList(resourceIds);
        List<Integer> subtract = CollectionKit.subtract(resourceIdList, roleResourceIds, ArrayList::new);
        if(!subtract.isEmpty()) {
            throw new SystemWarnException(Result.FAIL_CODE, "提交的权限中包含了您的身份中不存在的权限，请尝试重新选择并提交");
        }

        userPrincipalResourceService.deleteByPrincipalId(principalId);

        List<UserPrincipalResource> entities = resourceIdList.stream().map(resourceId -> {
           UserPrincipalResource entity = new UserPrincipalResource();
           entity.setUserPrincipalId(principalId);
           entity.setResourceId(resourceId);
           return entity;
        }).collect(Collectors.toList());

        userPrincipalResourceService.saveBatch(entities);
    }

    @Override
    public Set<String> changeResources(Integer roleId, Integer[] ids) {
        Set<Integer> beforeResourceIds = resourceService.findResourceByRoleId(roleId)
                .stream().map(Resource::getId).collect(Collectors.toSet());

        resourceService.changeResources(roleId, ids);
        List<User> users = userService.findByRoleId(roleId);
        Set<String> usernames = users.stream().map(User::getUsername).collect(Collectors.toSet());

        Set<Integer> principalIds = userPrincipalService.findPrincipalByOwners(usernames).stream().map(UserPrincipal::getId).collect(Collectors.toSet());

        HashSet<Integer> subIds = CollectionKit.subtract(beforeResourceIds, Arrays.asList(ids), HashSet::new);
        if(!subIds.isEmpty()) {
            userPrincipalResourceService.deleteByPrincipalIdsAndResourceIds(principalIds, subIds);
        }
        return usernames;
    }

    @Override
    public boolean awardPrincipal(String fromUsername, String toUsername, Integer userPrincipalId) {
        if(fromUsername.equals(toUsername)) {
            throw new SystemWarnException(Result.FAIL_CODE, "What's wrong with you man,Don't you know you can't empower yourself? Please say sorry to me! now!");
        }
        Set<Integer> userPrincipalIds = new HashSet<>();
        userPrincipalIds.add(userPrincipalId);
        return awardPrincipal(fromUsername, toUsername, userPrincipalIds);
    }

    @Override
    public boolean awardPrincipal(String fromUsername, String toUsername, Set<Integer> userPrincipalIds) {

        User userEntity = userService.findByUsername(fromUsername)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, fromUsername + "不存在，无法授权"));
        if(userEntity.getState().isLocked()) {
            throw new SystemWarnException(Result.FAIL_CODE, fromUsername + "被锁，无法授权");
        }
        userEntity = userService.findByUsername(toUsername)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, toUsername + "不存在，无法被授权"));

        if(userEntity.getState().isLocked()) {
            throw new SystemWarnException(Result.FAIL_CODE, toUsername + "被锁，无法被授权");
        }

        return awardRunAs0(fromUsername, toUsername, userPrincipalIds);
    }

    @Override
    public boolean takeBackPrincipal(String from, String to, Integer principalId) {
        UserPrincipal principal = userPrincipalService.getById(principalId)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "令牌不存在"));
        if(!principal.getOwner().equals(from)) {
            throw new SystemWarnException(Result.FAIL_CODE, "令牌不属于" + from + ", 无法被拿回");
        }

        return runAsService.giveBack(to, principalId);
    }

    @Override
    public List<Node<Resource>> getMenuTree(Set<Integer> principals) {
        Collection<Resource> resources = findResources(principals);

        Collection<Resource> resourcesAll = resourceService.list();


        return new NodeBuilder<Integer, Resource>().relation(relation).index(resourcesAll).toNode(resources);
    }


    @Override
    @Transactional
    public boolean updateRoleWithResource(Role role, List<Integer> resourceIds) {
        boolean success = roleService.updateById(role);
        resourceService.deleteRoleResource(role.getId());
        for(Integer id : resourceIds) {
            resourceService.saveRoleResource(role.getId(), id);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean register(User entity) {
        // 创建用户
        byte [] salt = HashKit.genSalt(hashedCredentialProperties.getSaltLen());
        String newPwdHash = HashKit.encodeHex(hashedCredentialProperties.getAlgorithm(), entity.getPassword().getBytes(), salt, hashedCredentialProperties.getIterations());
        entity.setSalt(HexKit.encodeHexStr(salt));
        entity.setPassword(newPwdHash);
        if(null == entity.getUseLanguage()) {
            entity.setUseLanguage(Language.ZH_CN);
        }
        userService.save(entity);

        // 创建主令牌
        UserPrincipal principal = new UserPrincipal();
        principal.setMain(true);
        principal.setOwner(entity.getUsername());
        principal.setPrincipalName("Main Principal");
        userPrincipalService.save(principal);

        // 给主令牌赋权
        List<Resource> roleResources = resourceService.findResourceByRoleId(entity.getRoleId());
        List<UserPrincipalResource> principalResources = roleResources.stream().map(resourceEntity -> {
            UserPrincipalResource principalResource = new UserPrincipalResource();
            principalResource.setResourceId(resourceEntity.getId());
            principalResource.setUserPrincipalId(principal.getId());
            return principalResource;
        }).collect(Collectors.toList());
        userPrincipalResourceService.saveBatch(principalResources);
        return true;
    }



    private boolean awardRunAs0(String fromUsername, String toUsername, Set<Integer> userPrincipalIds) {
        if(userPrincipalIds.size() == 0) {
            return true;
        }
        // 过滤掉 userPrincipalIds 中不属于 fromUsername的令牌，避免作弊
        List<UserPrincipal> list = userPrincipalService.lambdaQuery()
                .in(UserPrincipal::getId, userPrincipalIds)
                .eq(UserPrincipal::getOwner, fromUsername)
                .list();
        Set<Integer> filterAfterPrincipalIds = list.stream().map(UserPrincipal::getId)
                .filter(userPrincipalIds::contains)
                .collect(Collectors.toSet());

        // 把过滤后的令牌进行授权。
        if(filterAfterPrincipalIds.size() == 0) {
            return true;
        }
        runAsService.lambdaUpdate().eq(RunAs::getToUsername, toUsername)
                .in(RunAs::getPrincipalId, filterAfterPrincipalIds)
                .remove();

        List<RunAs> runAsList = userPrincipalIds.stream().map(userPrincipalId -> {
            RunAs runAsEntity = new RunAs();
            runAsEntity.setToUsername(toUsername);
            runAsEntity.setPrincipalId(userPrincipalId);
            return runAsEntity;
        }).collect(Collectors.toList());
        return runAsService.saveBatch(runAsList);
    }

    private User getUserAndCheckStatus(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, username + "用户不存在"));

        if(user.getState().isLocked()) {
            throw new SystemWarnException(Result.FAIL_CODE, "用户被锁定，无法进行操作");
        }

        return user;
    }
}
