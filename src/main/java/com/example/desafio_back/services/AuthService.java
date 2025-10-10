package com.example.desafio_back.services;

import com.example.desafio_back.dtos.LoginRequest;
import com.example.desafio_back.dtos.RegisterRequest;
import com.example.desafio_back.model.Account;
import com.example.desafio_back.model.User;
import com.example.desafio_back.repository.AccountRepository;
import com.example.desafio_back.repository.UserRepository;
import com.example.desafio_back.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @Transactional
    public void register(RegisterRequest request) {
        String cpfDigits = request.cpf().replaceAll("\\D", "");

        if (userRepository.existsByCpf(cpfDigits) || userRepository.existsByLogin(request.login())) {
            throw new IllegalArgumentException("CPF/Login already exists");
        }

        var user = userRepository.save(User.builder()
                .completeName(request.completeName())
                .cpf(cpfDigits)
                .login(request.login())
                .passwordHash(encoder.encode(request.password()))
                .build());

        accountRepository.save(Account.builder()
                .user(user)
                .currentBalance(BigDecimal.ZERO)
                .build());
    }

    public String login(LoginRequest request) {
        var user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwt.generate(user.getLogin());
    }
}
