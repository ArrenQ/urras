package com.chuang.urras.web.office.model;

import com.baomidou.mybatisplus.annotation.*;
import com.chuang.urras.support.enums.Gender;
import com.chuang.urras.support.enums.Language;
import com.chuang.urras.web.office.enums.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员 
 * </p>
 *
 * @author ath
 * @since 2020-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 盐
     */
    private String salt;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 性别
     */
    private Gender gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 办公手机
     */
    private String officePhone;

    /**
     * 私人手机
     */
    private String phone;

    /**
     * 用户状态
     */
    private UserStatus state;

    /**
     * 图片
     */
    private String picture;

    /**
     * mac地址
     */
    private String macAddress;

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     *
     */
    private Integer roleId;

    /**
     *
     */
    private Boolean bound;

    /**
     *
     */
    private LocalDateTime lastLoginTime;

    /**
     *
     */
    private Language useLanguage;

    private String productCode;

    /**
     * 最后登陆时IP
     */
    private String lastLoginIp;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;




}
