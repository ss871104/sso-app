package com.ss871104.oauth2security.security.kafka;

public class Oauth2Data {
    private String oAuth2Id;
    private String provider;
    private String email;
    private String fullName;
    private String familyName;
    private String givenName;

    public String getOAuth2Id() {
        return oAuth2Id;
    }

    public void setOAuth2Id(String oAuth2Id) {
        this.oAuth2Id = oAuth2Id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
}
