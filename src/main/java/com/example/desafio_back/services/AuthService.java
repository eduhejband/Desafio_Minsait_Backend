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

    private boolean isCpfValid(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!isCpfValid(request.cpf())) {
            throw new IllegalArgumentException("Invalid CPF");
        }
        if (userRepository.existsByCpf(request.cpf()) || userRepository.existsByLogin(request.login())) {
            throw new IllegalArgumentException("CPF/Login already exists");
        }

        var user = userRepository.save(User.builder()
                .completeName(request.completeName())
                .cpf(request.cpf())
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

    public static boolean isValidCPF(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) return false;
        if (cpf.chars().distinct().count() == 1) return false;

        int[] digits = cpf.chars().map(c -> c - '0').toArray();

        int sum = 0;
        for (int i = 0; i < 9; i++) sum += digits[i] * (10 - i);
        int check1 = 11 - (sum % 11);
        if (check1 >= 10) check1 = 0;
        if (check1 != digits[9]) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) sum += digits[i] * (11 - i);
        int check2 = 11 - (sum % 11);
        if (check2 >= 10) check2 = 0;
        return check2 == digits[10];
    }

}
