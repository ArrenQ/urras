package com.chuang.urras.web.office.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员登陆日子 
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_login_log")
public class LoginLog implements Serializable {

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
     * 真实名
     */
    private String realName;

    /**
     * 客户端host
     */
    private String clientHost;

    /**
     * 登陆时间
     */
    private LocalDateTime loginTime;

    /**
     * 是否登陆成功
     */
    private Boolean success;

    /**
     * 客户端mac地址
     */
    private String clientMac;

    /**
     * 设备
     */
    private String device;

    /**
     * 来源
     */
    private String referer;

    /**
     * 用户代理
     */
    private String userAgent;

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
