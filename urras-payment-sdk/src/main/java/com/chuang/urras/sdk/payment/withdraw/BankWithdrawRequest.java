package com.chuang.urras.sdk.payment.withdraw;

import com.chuang.urras.support.enums.Bank;
import lombok.Data;

@Data
public class BankWithdrawRequest extends WithdrawRequest {

    private Bank bank;

    private String province;

    private String city;

    private String branch;

}
