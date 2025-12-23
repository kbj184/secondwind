package com.secondwind.controller;

import com.secondwind.dto.UserDTO;
import com.secondwind.entity.UserAuth;
import com.secondwind.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;


    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody UserDTO userDTO) {

        System.out.println(userDTO.getEmail() + " " + userDTO.getPassword() );
        System.out.println("===========================================");
        if(loginService.joinProcess(userDTO)==true){
            return new ResponseEntity<Void>(HttpStatus.OK);
        }else{
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

    }

    @GetMapping("/emailcheck")
    public UserAuth emailChek(@RequestParam String email) {
        return  loginService.findByEmail(email);
    }
}
