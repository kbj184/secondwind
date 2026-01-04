package com.secondwind.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;

        // kakao_account가 없을 수 있으므로 null 체크
        Object accountObj = attribute.get("kakao_account");
        this.kakaoAccount = (accountObj instanceof Map) ? (Map<String, Object>) accountObj : null;

        // profile이 없을 수 있으므로 null 체크
        if (this.kakaoAccount != null) {
            Object profileObj = this.kakaoAccount.get("profile");
            this.profile = (profileObj instanceof Map) ? (Map<String, Object>) profileObj : null;
        } else {
            this.profile = null;
        }
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
        // profile이 없으면 기본 닉네임 반환
        if (profile != null && profile.containsKey("nickname")) {
            return profile.get("nickname").toString();
        }
        return "카카오사용자_" + attribute.get("id").toString();
    }
}
