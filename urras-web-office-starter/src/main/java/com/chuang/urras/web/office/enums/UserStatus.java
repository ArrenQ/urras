package com.chuang.urras.web.office.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * Created by Ath on 2017/6/28.
 */
public enum UserStatus {
    NORMAL(0),
    DISABLE(1);

    UserStatus(int code) {
        this.code = (byte)code;
    }
    @EnumValue
    private final byte code;

    public byte getCode() {
        return code;
    }

    public boolean isLocked() {
        return this == DISABLE;
    }
}
