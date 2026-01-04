package com.secondwind.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        this.profile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        // 이메일 권한이 없을 경우 카카오 ID로 대체 이메일 생성
        if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
            return kakaoAccount.get("email").toString();
        }
        // 이메일 권한이 없으면 카카오 ID로 가상 이메일 생성
        return "kakao_" + attribute.get("id").toString() + "@kakao.user";
    }

    @Override
    public String getName() {
        return profile.get("nickname").toString();
    }
}
