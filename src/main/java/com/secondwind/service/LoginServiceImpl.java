package com.secondwind.service;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserAuth findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Boolean joinProcess(UserDTO userDTO) {
        String email = userDTO.getEmail();
        String password = userDTO.getPassword();

        boolean isExt= userRepository.existsByEmail(email);

        if(isExt){
            return false;
        }

        UserAuth userAuth = new UserAuth();
        userAuth.setEmail(email);
        userAuth.setPassword(bCryptPasswordEncoder.encode(password));
        userAuth.setAuthProvider("local");
        userRepository.save(userAuth);

        return true;
    }

}
