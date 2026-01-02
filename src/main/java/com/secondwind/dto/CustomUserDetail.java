package com.secondwind.dto;

import com.secondwind.entity.UserAuth;
import com.secondwind.jwt.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetail implements UserDetails, AuthUser {

    private final UserAuth userAuth;

    public CustomUserDetail(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    @Override
    public String getProviderId() {
        return userAuth.getProviderId();
    }

    @Override
    public String getRole() {
        return userAuth.getRole();
    }

    @Override
    public Long getId() {
        return userAuth.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userAuth.getRole();
            }
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return userAuth.getPassword();
    }

    @Override
    public String getUsername() {
        return userAuth.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getEmail() {
        return userAuth.getEmail();
    }

    @Override
    public String getNickname() {
        return userAuth.getNickname();
    }

    @Override
    public String getNicknameImage() {
        return userAuth.getNicknameImage();
    }
}
