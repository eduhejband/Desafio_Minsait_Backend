package com.example.desafio_back.controllers;

import com.example.desafio_back.dtos.BalanceResponse;
import com.example.desafio_back.dtos.DepositRequest;
import com.example.desafio_back.dtos.PaymentRequest;
import com.example.desafio_back.repository.UserRepository;
import com.example.desafio_back.services.CommandService;
import com.example.desafio_back.services.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BankController {
    private final UserRepository userRepository;
    private final CommandService command;
    private final QueryService query;

    private Long getUserId() {
        String login = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByLogin(login).orElseThrow().getId();
    }

    @PostMapping("/deposits")
    public ResponseEntity<Void> deposit(@RequestBody DepositRequest r){
        command.deposit(getUserId(), r.value());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payments")
    public ResponseEntity<Void> pay(@RequestBody PaymentRequest r){
        command.pay(getUserId(), r.value());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public BalanceResponse balance(){
        return query.balance(getUserId());
    }
}
