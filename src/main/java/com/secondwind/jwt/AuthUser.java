package com.secondwind.jwt;

public interface AuthUser {
    String getProviderId();

    String getRole();

    Long getId();

    String getNickname();

    String getNicknameImage();

    String getEmail();
}