package com.chuang.urras.sdk.payment;

import com.chuang.urras.support.enums.PaymentType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求
 */
@Data
public class PaymentRequest {
    private String reference;
    private PaymentType type;
    private Long amount;
    private String username;
    private String clientIp;
}
