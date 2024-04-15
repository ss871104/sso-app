package com.ss871104.oauth2security.services;

import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.util.ServiceErrorException;

import java.util.List;

public interface RoleService {
    List<Role> findAllRoles() throws ServiceErrorException;
    void addRole(String roleName) throws ServiceErrorException;
    void updateRoleName(Long roleId, String roleName) throws ServiceErrorException;
    void deleteRole(Long roleId) throws ServiceErrorException;
    void addPermissionToRole(Long roleId, Long permissionId) throws ServiceErrorException;
    void deletePermissionFromRole(Long roleId, Long permissionId) throws ServiceErrorException;
}
