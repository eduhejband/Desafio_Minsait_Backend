package com.example.desafio_back.model;


import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String completeName;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(unique = true)
    private String login;

    private String passwordHash;
}
