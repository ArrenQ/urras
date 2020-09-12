package com.chuang.urras.boot.kit.annotation;

import com.chuang.urras.boot.kit.configuration.AsyncTaskPoolConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(AsyncTaskPoolConfiguration.class)
public @interface EnableAsyncTaskPool {
}
