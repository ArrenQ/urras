package com.chuang.urras.sdk;

import lombok.Data;

import javax.annotation.Nullable;
import java.util.Map;

@Data
public class SDKConfig {
    /** 商户号 */
    private String merchant;
    /** 私钥 */
    private String privateKey;
    /** 公钥 */
    private @Nullable
    String publicKey;
    /** 拓展信息 */
    private Map<String, Object> ext;
}
