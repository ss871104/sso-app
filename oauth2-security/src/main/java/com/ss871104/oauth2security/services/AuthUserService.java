package com.ss871104.oauth2security.services;

import com.ss871104.oauth2security.models.AuthUser;
import com.ss871104.oauth2security.util.ServiceErrorException;

import java.util.List;

public interface AuthUserService {
    List<AuthUser> findAllAuthUser() throws ServiceErrorException;
    AuthUser findAuthUserByOAuth2Id(String oAuth2Id) throws ServiceErrorException;
    void addRoleToAuthUser(Long roleId, String oAuth2Id) throws ServiceErrorException;
    void deleteRoleFromAuthUser(Long roleId, String oAuth2Id) throws ServiceErrorException;
    void blockAuthUser(String oAuth2Id) throws ServiceErrorException;
    void unblockAuthUser(String oAuth2Id) throws ServiceErrorException;
}
