package com.ss871104.oauth2security.services.impl;

import com.ss871104.oauth2security.models.AuthUser;
import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.repositories.AuthUserRepository;
import com.ss871104.oauth2security.repositories.RoleRepository;
import com.ss871104.oauth2security.services.AuthUserService;
import com.ss871104.oauth2security.util.ServiceErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(rollbackFor = ServiceErrorException.class)
public class AuthUserServiceImpl implements AuthUserService {
    @Autowired
    private AuthUserRepository authUserRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<AuthUser> findAllAuthUser() throws ServiceErrorException {
        try {
            List<AuthUser> authUserList = authUserRepository.findAll();
            if (authUserList.size() == 0) {
                throw new ServiceErrorException("Auth User not found!", HttpStatus.NOT_FOUND);
            }
            return authUserList;
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AuthUser findAuthUserByOAuth2Id(String oAuth2Id) throws ServiceErrorException {
        try {
            AuthUser authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            if (authUser == null) {
                throw new ServiceErrorException("Auth User not found!", HttpStatus.NOT_FOUND);
            }
            return authUser;
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void addRoleToAuthUser(Long roleId, String oAuth2Id) throws ServiceErrorException {
        try {
            Role roleToBeAdded = roleRepository.findRoleById(roleId);
            if (roleToBeAdded == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }

            AuthUser authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            Set<Role> roles = authUser.getRoles();
            roles.add(roleToBeAdded);
            authUser.setRoles(roles);

            authUserRepository.save(authUser);

        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteRoleFromAuthUser(Long roleId, String oAuth2Id) throws ServiceErrorException {
        try {
            Role roleToBeRemoved = roleRepository.findRoleById(roleId);
            if (roleToBeRemoved == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }

            AuthUser authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            Set<Role> roles = authUser.getRoles();
            roles.remove(roleToBeRemoved);
            authUser.setRoles(roles);
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void blockAuthUser(String oAuth2Id) throws ServiceErrorException {
        try {
            AuthUser authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            authUser.setBlocked(true);
            authUserRepository.save(authUser);
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void unblockAuthUser(String oAuth2Id) throws ServiceErrorException {
        try {
            AuthUser authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            authUser.setBlocked(false);
            authUserRepository.save(authUser);
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
