package com.example.desafio_back.dtos;

import java.math.BigDecimal;

public record ItemHistoric(String type, BigDecimal value, String date) {}