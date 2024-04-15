package com.ss871104.oauth2security.repositories;

import com.ss871104.oauth2security.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query(value = "SELECT r.* FROM role r WHERE r.id = :id", nativeQuery = true)
    Role findRoleById(@Param("id") Long id);

    @Query(value = "SELECT r.* FROM role r WHERE r.id in :ids", nativeQuery = true)
    Set<Role> findRolesByIds(@Param("ids") Set<Long> ids);
}
