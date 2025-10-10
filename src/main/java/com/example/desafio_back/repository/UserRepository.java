package com.example.desafio_back.repository;

import com.example.desafio_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    boolean existsByCpf(String cpf);
    boolean existsByLogin(String login);
}