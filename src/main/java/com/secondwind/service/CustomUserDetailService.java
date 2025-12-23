package com.secondwind.service;

import com.secondwind.dto.CustomUserDetail;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) {
        UserAuth userAuth = userRepository.findByEmail(email);
        return new CustomUserDetail(userAuth);
    }

}