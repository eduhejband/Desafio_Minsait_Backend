package com.example.desafio_back.services;

import com.example.desafio_back.dtos.LoginRequest;
import com.example.desafio_back.dtos.RegisterRequest;
import com.example.desafio_back.model.Account;
import com.example.desafio_back.model.User;
import com.example.desafio_back.repository.AccountRepository;
import com.example.desafio_back.repository.UserRepository;
import com.example.desafio_back.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    UserRepository userRepository;
    AccountRepository accountRepository;
    PasswordEncoder encoder;
    JwtUtil jwt;

    AuthService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        accountRepository = mock(AccountRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwt = mock(JwtUtil.class);
        service = new AuthService(userRepository, accountRepository, encoder, jwt);
    }

    @Test
    void register_createsUserWithEncodedPassword_andZeroBalanceAccount() {
        RegisterRequest req = new RegisterRequest("John Doe", "12345678901", "jdoe", "secret");
        when(userRepository.existsByCpf("12345678901")).thenReturn(false);
        when(userRepository.existsByLogin("jdoe")).thenReturn(false);
        when(encoder.encode("secret")).thenReturn("ENC(secret)");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });

        service.register(req);

        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCap.capture());
        User savedUser = userCap.getValue();
        assertThat(savedUser.getCompleteName()).isEqualTo("John Doe");
        assertThat(savedUser.getCpf()).isEqualTo("12345678901");
        assertThat(savedUser.getLogin()).isEqualTo("jdoe");
        assertThat(savedUser.getPasswordHash()).isEqualTo("ENC(secret)");

        ArgumentCaptor<Account> accCap = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCap.capture());
        Account savedAcc = accCap.getValue();
        assertThat(savedAcc.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedAcc.getUser()).isSameAs(savedUser);

        verifyNoInteractions(jwt);
    }

    @Test
    void register_rejectsInvalidCpf() {
        RegisterRequest bad = new RegisterRequest("John", "123", "j", "x");

        assertThatThrownBy(() -> service.register(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid CPF");

        verifyNoInteractions(userRepository, accountRepository, encoder, jwt);
    }

    @Test
    void register_rejectsExistingCpfOrLogin() {
        RegisterRequest req = new RegisterRequest("John", "12345678901", "jdoe", "x");
        when(userRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
        verifyNoInteractions(jwt);
    }

    @Test
    void login_returnsJwt_onValidCredentials() {
        LoginRequest req = new LoginRequest("jdoe", "secret");
        User dbUser = User.builder()
                .id(7L)
                .login("jdoe")
                .passwordHash("ENC(secret)")
                .build();

        when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(dbUser));
        when(encoder.matches("secret", "ENC(secret)")).thenReturn(true);
        when(jwt.generate("jdoe")).thenReturn("JWT123");

        String token = service.login(req);

        assertThat(token).isEqualTo("JWT123");
        verify(jwt).generate("jdoe");
    }

    @Test
    void login_throws_whenUserNotFound() {
        when(userRepository.findByLogin("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ghost", "x")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verifyNoInteractions(jwt);
    }

    @Test
    void login_throws_whenPasswordDoesNotMatch() {
        User dbUser = User.builder()
                .id(1L)
                .login("jdoe")
                .passwordHash("ENC(secret)")
                .build();

        when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(dbUser));
        when(encoder.matches("wrong", "ENC(secret)")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("jdoe", "wrong")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");

        verify(jwt, never()).generate(anyString());
    }

    @Test
    void isValidCPF_validatesCorrectly() {
        assertThat(AuthService.isValidCPF("52998224725")).isTrue();

        assertThat(AuthService.isValidCPF("123")).isFalse();

        assertThat(AuthService.isValidCPF("11111111111")).isFalse();

        assertThat(AuthService.isValidCPF("12345678901")).isFalse();

        assertThat(AuthService.isValidCPF(null)).isFalse();
    }
}
