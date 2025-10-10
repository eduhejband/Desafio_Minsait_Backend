package com.example.desafio_back.dtos;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record RegisterRequest(
        @NotBlank String completeName,
        @NotBlank @CPF String cpf,
        @NotBlank String login,
        @NotBlank String password
) {}
