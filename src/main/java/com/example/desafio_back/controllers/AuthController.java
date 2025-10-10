package com.example.desafio_back.controllers;

import com.example.desafio_back.dtos.LoginRequest;
import com.example.desafio_back.dtos.RegisterRequest;
import com.example.desafio_back.dtos.TokenResponse;
import com.example.desafio_back.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.desafio_back.services.AuthService.isValidCPF;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest r){
        auth.register(r);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid LoginRequest r){
        return new TokenResponse(auth.login(r));
    }
}
