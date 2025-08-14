package com.victor.VibeMatch.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    Role(String role){
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }
}
