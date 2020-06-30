package com.chuang.urras.web.office.model;

import com.baomidou.mybatisplus.annotation.*;
import com.chuang.urras.support.enums.CRUD;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作时间 
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 操作者
     */
    private String operator;

    /**
     * 操作类型
     */
    private CRUD crudType;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 数据类
     */
    private String dataClass;

    /**
     * 差异
     */
    private String difference;

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
