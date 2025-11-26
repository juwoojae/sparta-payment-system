package com.sparta.payment_system.controller;

import com.sparta.payment_system.dto.auth.CreateUserRequest;
import com.sparta.payment_system.dto.auth.CreateUserResponse;
import com.sparta.payment_system.dto.auth.PostLoginRequest;
import com.sparta.payment_system.dto.auth.PostLoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth")
public class AuthViewController {

    @GetMapping("/login")
    public String loginForm() {
        return "loginForm";
    }

	@GetMapping("/register")
	public String registerForm() {
		return "registerForm";
	}
}
