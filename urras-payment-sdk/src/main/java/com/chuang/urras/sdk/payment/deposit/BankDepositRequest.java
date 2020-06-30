package com.chuang.urras.sdk.payment.deposit;

import com.chuang.urras.support.enums.Bank;
import lombok.Data;

@Data
public class BankDepositRequest extends DepositRequest {
    private String realname;
    private Bank bank;
    private String paymentAccount;
}
