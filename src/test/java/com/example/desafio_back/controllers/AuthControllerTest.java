package com.example.desafio_back.controllers;

import com.example.desafio_back.dtos.LoginRequest;
import com.example.desafio_back.dtos.RegisterRequest;
import com.example.desafio_back.dtos.TokenResponse;
import com.example.desafio_back.services.AuthService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    AuthService authService = mock(AuthService.class);
    AuthController controller = new AuthController(authService);

    @Test
    void register_callsService() {
        RegisterRequest req = new RegisterRequest("John Doe", "12345678901", "jdoe", "secret");

        controller.register(req);

        verify(authService).register(req);
    }

    @Test
    void login_returnsTokenResponse() {
        LoginRequest req = new LoginRequest("jdoe", "secret");
        when(authService.login(req)).thenReturn("JWT123");

        TokenResponse resp = controller.login(req);

        assertThat(resp.token()).isEqualTo("JWT123");
        verify(authService).login(req);
    }
}
