package com.chuang.urras.support.exception;

import java.util.Optional;

/**
 * 不能将消息传递到客户端的异常。
 *
 * @author ath
 * @date 2016/7/7
 */
public class SystemErrorException extends CodeException {
    public SystemErrorException(int code, String msg) {
        super(code, msg);
    }
    public SystemErrorException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public static boolean hasSystemErrorException(Throwable e) {
        if(e instanceof SystemErrorException) {
            return true;
        }
        Throwable throwable = e;
        while(null != throwable) {
            throwable = throwable.getCause();
            if(throwable instanceof SystemErrorException) {
                return true;
            }
        }
        return false;
    }

    public static Optional<String> getSystemErrorExceptionMessage(Throwable e) {
        if(e instanceof SystemErrorException) {
            return Optional.of(e.getMessage());
        }
        Throwable throwable = e;
        while(null != throwable) {
            throwable = throwable.getCause();
            if(throwable instanceof SystemErrorException) {
                return Optional.of(throwable.getMessage());
            }
        }
        return Optional.empty();
    }
}
