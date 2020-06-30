package com.chuang.urras.sdk;

import com.chuang.urras.toolskit.third.apache.httpcomponents.Request;

public interface CredentialAlgorithm {
    CredentialAlgorithm NONE = (Request request, Object arg) -> {};

    CredentialAlgorithm QUERY_STR_MD5 = (Request request, Object arg) -> {};

    CredentialAlgorithm PARAM_VALUE_MD5 = (Request request, Object arg) -> {};

    void sign(Request request, Object arg);
}
