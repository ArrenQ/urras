package com.chuang.urras.web.shiro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "urras.hashed-credential")
public class HashedCredentialProperties {
    private String algorithm = "MD5";
    private int iterations = 3;
    private int saltLen = 6;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getSaltLen() {
        return saltLen;
    }

    public void setSaltLen(int saltLen) {
        this.saltLen = saltLen;
    }

}
