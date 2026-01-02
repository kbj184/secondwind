package com.secondwind.dto;

import com.secondwind.jwt.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User, AuthUser {

    private final UserDTO userDTO;

    public CustomOAuth2User(UserDTO userDTO) {

        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {

        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {

                return userDTO.getRole();
            }
        });

        return collection;
    }

    @Override
    public String getName() {

        return userDTO.getName();
    }

    @Override
    public String getProviderId() {

        return userDTO.getProviderId();
    }

    @Override
    public String getRole() {

        return userDTO.getRole();
    }

    @Override
    public Long getId() {
        return userDTO.getId();
    }

    @Override
    public String getEmail() {
        return userDTO.getEmail();
    }

    @Override
    public String getNickname() {
        return userDTO.getNickname();
    }

    @Override
    public String getNicknameImage() {
        return userDTO.getNicknameImage();
    }
}
