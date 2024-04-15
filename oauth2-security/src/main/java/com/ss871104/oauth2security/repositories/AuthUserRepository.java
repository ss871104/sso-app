package com.ss871104.oauth2security.repositories;

import com.ss871104.oauth2security.models.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, String> {
    @Query(value = "SELECT u.* FROM auth_user u WHERE u.oauth2_id = :oAuth2Id", nativeQuery = true)
    AuthUser findAuthUserByOAuth2Id(@Param("oAuth2Id") String oAuth2Id);
}
