package com.chuang.urras.sdk.payment;

import java.util.Optional;

public interface ConfigLoadPolicy {

    Optional<PaymentPlatformConfig> loadConfig(PaymentRequest request, PaymentSDK sdk);

    Optional<PaymentPlatformConfig> loadConfig(String key);
}
