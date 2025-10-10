package com.example.desafio_back.repository;

import com.example.desafio_back.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountIdOrderByDateTimeDesc(Long accountId);

}