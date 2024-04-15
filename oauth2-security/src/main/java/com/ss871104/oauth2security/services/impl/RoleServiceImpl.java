package com.ss871104.oauth2security.services.impl;

import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.repositories.PermissionRepository;
import com.ss871104.oauth2security.repositories.RoleRepository;
import com.ss871104.oauth2security.security.redis.PermissionRedisService;
import com.ss871104.oauth2security.services.RoleService;
import com.ss871104.oauth2security.util.ServiceErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(rollbackFor = ServiceErrorException.class)
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionRedisService permissionRedisService;

    @Override
    public List<Role> findAllRoles() throws ServiceErrorException {
        try {
            List<Role> roleList = roleRepository.findAll();
            if (roleList.size() == 0) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }
            return roleList;
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void addRole(String roleName) throws ServiceErrorException {
        try {
            Role role = new Role();
            role.setName(roleName);

            roleRepository.save(role);
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void updateRoleName(Long roleId, String roleName) throws ServiceErrorException {
        try {
            Role role = roleRepository.findRoleById(roleId);
            if (role == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }
            role.setName(roleName);

            roleRepository.save(role);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteRole(Long roleId) throws ServiceErrorException {
        try {
            Role role = roleRepository.findRoleById(roleId);
            if (role == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }

            Set<Permission> permissionSet = role.getPermissions();
            permissionSet.forEach(p -> {
                p.getRoles().remove(role);
                permissionRepository.save(p);
            });

            roleRepository.delete(role);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void addPermissionToRole(Long roleId, Long permissionId) throws ServiceErrorException {
        try {
            Role role = roleRepository.findRoleById(roleId);
            if (role == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }
            Permission permission = permissionRepository.findPermissionById(permissionId);
            if (permission == null) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }

            role.getPermissions().add(permission);

            roleRepository.save(role);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deletePermissionFromRole(Long roleId, Long permissionId) throws ServiceErrorException {
        try {
            Role role = roleRepository.findRoleById(roleId);
            if (role == null) {
                throw new ServiceErrorException("Role not found!", HttpStatus.NOT_FOUND);
            }
            Permission permission = permissionRepository.findPermissionById(permissionId);
            if (permission == null) {
                throw new ServiceErrorException("Permission not found!", HttpStatus.NOT_FOUND);
            }

            role.getPermissions().remove(permission);

            roleRepository.save(role);
            permissionRedisService.removeAllowPermissionUrlsByRoleNames();
        } catch (Exception e) {
            throw new ServiceErrorException("DB ERROR!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
