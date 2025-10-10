package com.example.desafio_back.controllers;

import com.example.desafio_back.dtos.BalanceResponse;
import com.example.desafio_back.dtos.DepositRequest;
import com.example.desafio_back.dtos.PaymentRequest;
import com.example.desafio_back.model.User;
import com.example.desafio_back.repository.UserRepository;
import com.example.desafio_back.services.CommandService;
import com.example.desafio_back.services.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BankControllerTest {

    UserRepository userRepository;
    CommandService commandService;
    QueryService queryService;

    BankController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        commandService = mock(CommandService.class);
        queryService = mock(QueryService.class);
        controller = new BankController(userRepository, commandService, queryService);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("jdoe", null)
        );

        User fakeUser = User.builder().id(42L).login("jdoe").build();
        when(userRepository.findByLogin("jdoe")).thenReturn(Optional.of(fakeUser));
    }

    @Test
    void deposit_callsServiceWithCorrectUserId() {
        DepositRequest req = new DepositRequest(new BigDecimal("100.00"));

        controller.deposit(req);

        verify(commandService).deposit(42L, new BigDecimal("100.00"));
    }

    @Test
    void pay_callsServiceWithCorrectUserId() {
        PaymentRequest req = new PaymentRequest(new BigDecimal("50.00"));

        controller.pay(req);

        verify(commandService).pay(42L, new BigDecimal("50.00"));
    }

    @Test
    void balance_returnsResponseFromService() {
        BalanceResponse mockResponse = new BalanceResponse(new BigDecimal("500.00"), java.util.List.of());
        when(queryService.balance(42L)).thenReturn(mockResponse);

        BalanceResponse resp = controller.balance();

        assertThat(resp.totalBalance()).isEqualByComparingTo("500.00");
        verify(queryService).balance(42L);
    }
}
