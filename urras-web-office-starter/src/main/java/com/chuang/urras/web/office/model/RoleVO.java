package com.chuang.urras.web.office.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel
@Data
public class RoleVO implements Serializable {

    @ApiModelProperty("角色ID")
    private Integer id;
    @ApiModelProperty("角色名")
    private String role;
    @ApiModelProperty("角色说明")
    private String description;
    @ApiModelProperty("是否可用")
    private Boolean available;

}
