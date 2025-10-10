package com.example.desafio_back.model;

import com.example.desafio_back.enums.Type;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(precision = 19, scale = 2)
    private BigDecimal value;

    private LocalDateTime dateTime;
}
