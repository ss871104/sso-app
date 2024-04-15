package com.ss871104.oauth2security.services;

import com.ss871104.oauth2security.dtos.input.AddPermissionDto;
import com.ss871104.oauth2security.dtos.input.UpdatePermissionDto;
import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.util.ServiceErrorException;

import java.util.List;

public interface PermissionService {
    List<Permission> findAllPermissions() throws ServiceErrorException;
    List<Permission> findPermissionsByRoleId(Long roleId) throws ServiceErrorException;
    void addPermission(AddPermissionDto addPermissionDto) throws ServiceErrorException;
    void updatePermission(UpdatePermissionDto updatePermissionDto) throws ServiceErrorException;
    void deletePermission(Long permissionId) throws ServiceErrorException;
}
