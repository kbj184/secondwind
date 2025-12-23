package com.secondwind.service;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;

public interface LoginService {
    UserAuth findByEmail(String email);
    Boolean joinProcess(UserDTO userDTO);
}
