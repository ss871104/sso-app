package com.ss871104.oauth2security.dtos.input;

public class UpdateRoleFromAuthUserDto {
    private Long roleId;
    private String oAuth2Id;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getOAuth2Id() {
        return oAuth2Id;
    }

    public void setOAuth2Id(String oAuth2Id) {
        this.oAuth2Id = oAuth2Id;
    }

}
