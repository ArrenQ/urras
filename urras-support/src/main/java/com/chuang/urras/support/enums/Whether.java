package com.chuang.urras.support.enums;

public enum Whether {
    YES, NO, UNKNOWN;

    public boolean isSuccess() {
        return YES == this;
    }
}
