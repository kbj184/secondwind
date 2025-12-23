package com.secondwind.jwt;

import org.springframework.security.core.GrantedAuthority;

public interface AuthUser {
    String getProviderId();
    String getRole();
    Long getId();
}