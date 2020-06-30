package com.chuang.urras.sdk.payment.withdraw;

import com.chuang.urras.sdk.payment.PaymentRequest;
import lombok.Data;

import java.util.Optional;

@Data
public class WithdrawRequest extends PaymentRequest {
    private String account;

    private String realname;
}
