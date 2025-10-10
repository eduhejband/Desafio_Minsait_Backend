package com.example.desafio_back.services;

import com.example.desafio_back.dtos.BalanceResponse;
import com.example.desafio_back.dtos.CacheBalance;
import com.example.desafio_back.dtos.ItemHistoric;
import com.example.desafio_back.enums.Type;
import com.example.desafio_back.model.Account;
import com.example.desafio_back.model.Transaction;
import com.example.desafio_back.repository.AccountRepository;
import com.example.desafio_back.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, CacheBalance> redis;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Transactional(readOnly = true)
    public BalanceResponse balance(Long userId) {
        String key = CommandService.cacheKey(userId);

        CacheBalance cached = redis.opsForValue().get(key);
        if (cached != null && cached.getTotalBalance() != null) {
            return new BalanceResponse(cached.getTotalBalance(), cached.getHistoric());
        }

        Account acc = accountRepository.findByUserId(userId).orElseThrow();
        BigDecimal total = acc.getCurrentBalance() == null ? BigDecimal.ZERO : acc.getCurrentBalance();

        List<Transaction> txs = transactionRepository.findByAccountIdOrderByDateTimeDesc(acc.getId());

        List<ItemHistoric> items = txs.stream()
                .map(t -> new ItemHistoric(
                        t.getType() == Type.DEPOSIT ? "deposit" : "payment",
                        t.getValue(),
                        t.getDateTime().format(FMT)
                ))
                .toList();

        redis.opsForValue().set(key, new CacheBalance(total, items));

        return new BalanceResponse(total, items);
    }

}