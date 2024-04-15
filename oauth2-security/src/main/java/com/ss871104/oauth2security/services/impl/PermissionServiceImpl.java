package com.ss871104.oauth2security.services.impl;

import com.ss871104.oauth2security.dtos.input.AddPermissionDto;
import com.ss871104.oauth2security.dtos.input.UpdatePermissionDto;
import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.repositories.PermissionRepository;
import com.ss871104.oauth2security.repositories.RoleRepository;
import com.ss871104.oauth2security.security.redis.PermissionRedisService;
import com.ss871104.oauth2security.services.PermissionService;
import com.ss871104.oauth2security.util.ServiceErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionRedisService permissionRedisService;

    @Override
    public List<Permission> findAllPermissions() throws ServiceErrorException {
        try {
            List<Permission> permissionList = permissionRepository.findAll();
            if (permissionList.size() == 0) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }
            return permissionList;
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Permission> findPermissionsByRoleId(Long roleId) throws ServiceErrorException {
        try {
            List<Permission> permissionList = permissionRepository.findPermissionsByRoleId(roleId);
            if (permissionList.size() == 0) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }
            return permissionList;
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void addPermission(AddPermissionDto addPermissionDto) throws ServiceErrorException {
        try {
            String url = addPermissionDto.getUrl();
            String description = addPermissionDto.getDescription();
            Set<Long> roleIds = addPermissionDto.getRoleIds();

            Permission permission = new Permission();
            Set<Role> roleSet = roleRepository.findRolesByIds(roleIds);
            if (roleSet.size() == 0) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }

            permission.setUrl(url);
            permission.setDescription(description);
            permission.setRoles(roleSet);

            permissionRepository.save(permission);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void updatePermission(UpdatePermissionDto updatePermissionDto) throws ServiceErrorException {
        try {
            Long permissionId = updatePermissionDto.getPermissionId();
            String url = updatePermissionDto.getUrl();
            String description = updatePermissionDto.getDescription();

            Permission permission = permissionRepository.findPermissionById(permissionId);
            if (permissionId == null) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }

            permission.setUrl(url);
            permission.setDescription(description);

            permissionRepository.save(permission);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deletePermission(Long permissionId) throws ServiceErrorException {
        try {
            Permission permission = permissionRepository.findPermissionById(permissionId);
            if (permissionId == null) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }

            Set<Role> roleSet = permission.getRoles();
            roleSet.forEach(r -> {
                r.getPermissions().remove(permission);
                roleRepository.save(r);
            });

            permissionRepository.delete(permission);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
