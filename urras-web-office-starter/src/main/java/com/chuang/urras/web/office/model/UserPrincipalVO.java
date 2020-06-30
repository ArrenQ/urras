package com.chuang.urras.web.office.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class UserPrincipalVO {

    @ApiModelProperty("令牌ID")
    private Integer id;
    @ApiModelProperty("令牌别名")
    private String principalName;
    @ApiModelProperty("令牌所属")
    private String owner;
    @ApiModelProperty("是否为主令牌")
    private Boolean main;

}
