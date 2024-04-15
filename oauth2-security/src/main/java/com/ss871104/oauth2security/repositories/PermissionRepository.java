package com.ss871104.oauth2security.repositories;

import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    @Query(value = "SELECT p.* FROM permission p WHERE p.id = :id", nativeQuery = true)
    Permission findPermissionById(@Param("id") Long id);

    @Query(value = "SELECT p.* FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = :roleId",
            nativeQuery = true)
    List<Permission> findPermissionsByRoleId(Long roleId);

    @Query(value = "SELECT p.* FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN role r ON r.id = rp.role_id " +
            "WHERE r.name IN :roleNames",
            nativeQuery = true)
    Set<Permission> findPermissionsByRoleNames(@Param("roleNames") Set<String> roleNames);
}
