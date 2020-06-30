package com.chuang.urras.sdk;


import com.alibaba.fastjson.JSONObject;
import com.chuang.urras.toolskit.third.apache.httpcomponents.Request;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpSDKProxy implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -1L;

    private final Class<?> remoteSDKClass;

    public HttpSDKProxy(Class remoteSDKClass) {
        this.remoteSDKClass = remoteSDKClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        RemoteSDK sdk = remoteSDKClass.getAnnotation(RemoteSDK.class);
        RemoteSDKApi api = method.getAnnotation(RemoteSDKApi.class);

//        CompletableFuture<Object> obj = Request.newBuilder()
//                .url(sdk.domain() + api.value())
//                .method(mapping.method())
//                .parameter(toMap(method, args))
//                .build()
//                .asyncExecuteAsString()
//                .thenApply(s -> {
//                    Class clazz = method.getReturnType();
//                    if(clazz == CompletableFuture.class) {
//                        clazz = clazz.getComponentType();
//                    }
//                    return JSONObject.parseObject(s, clazz);
//                });
//
//        if (method.getReturnType() == CompletableFuture.class) {
//            return obj;
//        } else {
//            return obj.join();
//        }
        return null;
    }

    private Map<String, String> toMap(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();

        Map<String, String> params = new HashMap<>();
        for(int i = 0; i < parameters.length; i++) {
            params.put(parameters[i].getName(), args[i].toString());
        }
        return params;
    }

}
