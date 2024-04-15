package com.ss871104.oauth2security.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="auth_user")
public class AuthUser {
    @Id
    @Column(name = "oauth2_id")
    private String oAuth2Id;
    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "auth_user_role",
            joinColumns = @JoinColumn(name = "oauth2_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonManagedReference
    private Set<Role> roles = new HashSet<>();
    @Column(name = "is_blocked")
    private boolean isBlocked;

    public String getoAuth2Id() {
        return oAuth2Id;
    }

    public void setoAuth2Id(String oAuth2Id) {
        this.oAuth2Id = oAuth2Id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
