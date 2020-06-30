package com.chuang.urras.toolskit.third.spring.rest.annotation;


import com.chuang.urras.toolskit.third.apache.httpcomponents.HttpMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Mapping {
    String value() default "/";

    HttpMethod method();
}
