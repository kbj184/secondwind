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
        UserAuth userAuth = userRepository.findByEmail(email);

        if (userAuth == null) {
            throw new RuntimeException("User not found");
        }

        userAuth.setNickname(userDTO.getNickname());
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
}
