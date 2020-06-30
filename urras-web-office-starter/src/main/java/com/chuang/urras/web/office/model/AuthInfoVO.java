package com.chuang.urras.web.office.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiModel
@Data
public class AuthInfoVO {

    @ApiModelProperty("当前使用的令牌")
    private Set<UserPrincipalVO> principals;
    @ApiModelProperty("是否正使用其他令牌")
    private boolean runAs;

    @ApiModelProperty("登录用户")
    private UserVO user;
    @ApiModelProperty("登录用户的角色")
    private RoleVO role;

    @ApiModelProperty("被授权的令牌集")
    private List<UserPrincipalVO> awardedPrincipals;

    @ApiModelProperty("我的所有令牌")
    private List<UserPrincipalVO> myPrincipals;

    @ApiModelProperty("令牌授权信息，key为令牌id, value为被授予的用户集合")
    private Map<Integer, Set<String>> authMap;

}
