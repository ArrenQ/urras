package com.chuang.urras.sdk;

import java.util.Map;

public class RemoteSDKManager {

    private final Map<String, HttpSDKConfigurer> sdkConfigurerMap;

    private final Class markerInterface;

    public RemoteSDKManager(Map<String, HttpSDKConfigurer> sdkConfigurerMap, Class markerInterface) {
        this.sdkConfigurerMap = sdkConfigurerMap;
        this.markerInterface = markerInterface;
    }



}
