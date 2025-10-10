package com.example.desafio_back.dtos;

import java.math.BigDecimal;
import java.util.List;

public record BalanceResponse(BigDecimal totalBalance, List<ItemHistoric> Historic) {}