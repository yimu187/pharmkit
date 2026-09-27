package com.pharmkit.controller;

import com.pharmkit.dto.UserResponse;
import com.pharmkit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
class MeController {

    private final UserService users;

    MeController(UserService users) {
        this.users = users;
    }

    @GetMapping
    UserResponse me() {
        return UserResponse.from(users.requireCurrent());
    }
}
