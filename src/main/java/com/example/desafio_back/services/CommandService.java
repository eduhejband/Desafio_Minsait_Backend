package com.example.desafio_back.services;

import com.example.desafio_back.dtos.CacheBalance;
import com.example.desafio_back.dtos.ItemHistoric;
import com.example.desafio_back.enums.Type;
import com.example.desafio_back.model.Account;
import com.example.desafio_back.model.Transaction;
import com.example.desafio_back.repository.AccountRepository;
import com.example.desafio_back.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, CacheBalance> redis;

    private static final BigDecimal INTEREST = new BigDecimal("0.102");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        Account acc = accountRepository.findByUserId(userId).orElseThrow();
        BigDecimal balance = acc.getCurrentBalance() == null ? BigDecimal.ZERO : acc.getCurrentBalance();

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal interest = balance.abs().multiply(INTEREST);
            acc.setCurrentBalance(balance.subtract(interest).add(amount));
        } else {
            acc.setCurrentBalance(balance.add(amount));
        }

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(acc)
                .type(Type.DEPOSIT)
                .value(amount)
                .dateTime(LocalDateTime.now())
                .build());

        accountRepository.save(acc);

        writeThrough(userId, acc, tx);
    }

    @Transactional
    public void pay(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        Account acc = accountRepository.findByUserId(userId).orElseThrow();
        BigDecimal balance = acc.getCurrentBalance() == null ? BigDecimal.ZERO : acc.getCurrentBalance();
        acc.setCurrentBalance(balance.subtract(amount));

        Transaction tx = transactionRepository.save(Transaction.builder()
                .account(acc)
                .type(Type.PAYMENT)
                .value(amount)
                .dateTime(LocalDateTime.now())
                .build());

        accountRepository.save(acc);

        writeThrough(userId, acc, tx);
    }

    private void writeThrough(Long userId, Account acc, Transaction newTx) {
        String key = cacheKey(userId);

        CacheBalance cb = redis.opsForValue().get(key);
        if (cb == null){
            cb = new CacheBalance();
        }

        cb.setTotalBalance(acc.getCurrentBalance());

        String type = (newTx.getType() == Type.DEPOSIT) ? "deposit" : "payment";
        cb.getHistoric().add(0, new ItemHistoric(
                type,
                newTx.getValue(),
                newTx.getDateTime().format(FMT)
        ));

        if (cb.getHistoric().size() > 200) {
            cb.getHistoric().remove(cb.getHistoric().size() - 1);
        }

        redis.opsForValue().set(key, cb);
    }

    public static String cacheKey(Long userId) {
        return "balance:" + userId;
    }
}
