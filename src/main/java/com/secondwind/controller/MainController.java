package com.secondwind.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String index() {

        System.out.println("===============================================================12345");

        return "main route";
    }

}
