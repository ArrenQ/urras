package com.chuang.urras.support.exception;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * 可以将消息传递到客户端的异常
 *
 * @author ath
 * @date 2016/7/7
 */
public class SystemWarnException extends CodeException {
    public SystemWarnException(String msg) {
        super(-1, msg);
    }
    public SystemWarnException(int code, String msg) {
        super(code, msg);
    }
    public SystemWarnException(int code, String msg, Throwable e) {
        super(code, msg, e);
    }

    public SystemWarnException(int code, String pattern, Object... args) {
        this(code, MessageFormat.format(pattern, args));
    }

    public static boolean hasSystemWarnException(Throwable e) {
        if(e instanceof SystemWarnException) {
            return true;
        }
        Throwable throwable = e;
        while(null != throwable) {
            throwable = throwable.getCause();
            if(throwable instanceof SystemWarnException) {
                return true;
            }
        }
        return false;
    }

    public static Optional<String> getSystemWarnExceptionMessage(Throwable e) {
        if(e instanceof SystemWarnException) {
            return Optional.of(e.getMessage());
        }
        Throwable throwable = e;
        while(null != throwable) {
            throwable = throwable.getCause();
            if(throwable instanceof SystemWarnException) {
                return Optional.of(throwable.getMessage());
            }
        }
        return Optional.empty();
    }
}
