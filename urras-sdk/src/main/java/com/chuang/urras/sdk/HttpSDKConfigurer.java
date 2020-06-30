package com.chuang.urras.sdk;

import lombok.Data;

import javax.annotation.Nullable;

@Data
public class HttpSDKConfigurer {

    /** host，比如：http://api.anarres.life */
    private String apiHost;
    /** 平台名称 */
    private String platformName;
    /** 代理 */
    private @Nullable
    String proxy;
    /** 加密手段 */
    private CredentialAlgorithm signAlgorithm = CredentialAlgorithm.NONE;
}
