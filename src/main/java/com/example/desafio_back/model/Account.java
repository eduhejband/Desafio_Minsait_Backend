package com.example.desafio_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private User user;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentBalance;
}
