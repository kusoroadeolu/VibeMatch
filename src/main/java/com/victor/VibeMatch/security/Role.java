package com.victor.VibeMatch.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;

    Role(String role){
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }
}
