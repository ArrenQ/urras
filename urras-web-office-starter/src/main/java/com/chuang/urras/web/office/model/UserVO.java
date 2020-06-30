package com.chuang.urras.web.office.model;

import com.chuang.urras.support.enums.Gender;
import com.chuang.urras.support.enums.Language;
import com.chuang.urras.web.office.enums.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("用户信息")
@Data
public class UserVO implements java.io.Serializable {

    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("性别")
    private Gender gender;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("办公电话")
    private String officePhone;
    @ApiModelProperty("个人电话")
    private String phone;
    @ApiModelProperty("用户状态")
    private UserStatus state;
    @ApiModelProperty("头像图片地址")
    private String picture;
    @ApiModelProperty("默认使用语言")
    private Language useLanguage;
    @ApiModelProperty("角色ID")
    private Integer roleId;

    @ApiModelProperty("备注")
    private String remark;

}