package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
    @GetMapping("/my")
    public UserDTO my() {

        System.out.println("===========================================my1");
        System.out.println("===========================================my2");
        System.out.println("===========================================my3");
        UserDTO  userDTO = new UserDTO();
        userDTO.setRole("admin2");
        userDTO.setName("admin3");
        return userDTO;
    }
}
