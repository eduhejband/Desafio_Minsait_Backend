package com.example.desafio_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheBalance {
    private BigDecimal totalBalance;
    private List<ItemHistoric> historic = new ArrayList<>();
}