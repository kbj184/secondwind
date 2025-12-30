package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/profile")
    public UserDTO updateProfile(@RequestBody UserDTO userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("UserController: Profile update requested by " + email);

        UserAuth userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        // 닉네임 검증
        String nickname = userDTO.getNickname();
        validateNickname(nickname, userAuth.getId());

        userAuth.setNickname(nickname);
        userAuth.setNicknameImage(userDTO.getNicknameImage());
        userRepository.save(userAuth);

        UserDTO response = new UserDTO();
        response.setId(userAuth.getId());
        response.setEmail(userAuth.getEmail());
        response.setNickname(userAuth.getNickname());
        response.setNicknameImage(userAuth.getNicknameImage());
        response.setRole(userAuth.getRole());

        return response;
    }

    @GetMapping("/check-nickname")
    public boolean checkNickname(@RequestParam String nickname) {
        // 닉네임 중복 여부 반환 (true = 사용 가능, false = 중복)
        return !userRepository.existsByNickname(nickname);
    }

    private void validateNickname(String nickname, Long currentUserId) {
        // 1. null 또는 빈 값 체크
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        // 2. 길이 체크 (2-10자)
        if (nickname.length() < 2) {
            throw new IllegalArgumentException("닉네임은 최소 2자 이상이어야 합니다.");
        }
        if (nickname.length() > 10) {
            throw new IllegalArgumentException("닉네임은 최대 10자까지 가능합니다.");
        }

        // 3. 특수문자 체크 (한글, 영문, 숫자만 허용)
        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자만 사용 가능합니다.");
        }

        // 4. 중복 체크 (자신의 닉네임은 제외)
        UserAuth existingUser = userRepository.findByNickname(nickname);
        if (existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 5. 금지어 체크
        String[] bannedWords = { "관리자", "운영자", "admin", "root", "system" };
        String lowerNickname = nickname.toLowerCase();
        for (String banned : bannedWords) {
            if (lowerNickname.contains(banned)) {
                throw new IllegalArgumentException("사용할 수 없는 닉네임입니다.");
            }
        }
    }
}
