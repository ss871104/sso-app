package com.ss871104.oauth2security.security.token;

public class TokenData {
    private String accessToken;
    private String oAuth2Id;
    private String roles;
    private String provider;
    private String device;
    private String host;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getOAuth2Id() {
        return oAuth2Id;
    }

    public void setOAuth2Id(String oAuth2Id) {
        this.oAuth2Id = oAuth2Id;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String role) {
        this.roles = role;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
