package com.example.desafio_back.services;

import com.example.desafio_back.dtos.BalanceResponse;
import com.example.desafio_back.dtos.CacheBalance;
import com.example.desafio_back.dtos.ItemHistoric;
import com.example.desafio_back.enums.Type;
import com.example.desafio_back.model.Account;
import com.example.desafio_back.model.Transaction;
import com.example.desafio_back.repository.AccountRepository;
import com.example.desafio_back.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueryServiceTest {

    AccountRepository accountRepository;
    TransactionRepository transactionRepository;

    @SuppressWarnings("unchecked")
    RedisTemplate<String, CacheBalance> redis;

    @SuppressWarnings("unchecked")
    ValueOperations<String, CacheBalance> valueOps;

    QueryService service;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        redis = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);

        when(redis.opsForValue()).thenReturn(valueOps);

        service = new QueryService(accountRepository, transactionRepository, redis);
    }

    @Test
    void balance_returnsFromCache_whenPresent() {
        Long userId = 7L;
        String key = CommandService.cacheKey(userId);

        CacheBalance cached = new CacheBalance(
                new BigDecimal("123.45"),
                List.of(
                        new ItemHistoric("deposit", new BigDecimal("50.00"), "01-01-2025 10:00:00"),
                        new ItemHistoric("payment", new BigDecimal("20.00"), "02-01-2025 09:00:00")
                )
        );

        when(valueOps.get(key)).thenReturn(cached);

        BalanceResponse resp = service.balance(userId);

        assertThat(resp.totalBalance()).isEqualByComparingTo("123.45");
        assertThat(resp.Historic()).hasSize(2);
        assertThat(resp.Historic().get(0))
                .usingRecursiveComparison()
                .isEqualTo(new ItemHistoric("deposit", new BigDecimal("50.00"), "01-01-2025 10:00:00"));

        verifyNoInteractions(accountRepository, transactionRepository);
        verify(valueOps, never()).set(anyString(), any(CacheBalance.class));
    }

    @Test
    void balance_buildsFromDatabase_andCaches_whenCacheMiss() {
        Long userId = 5L;
        String key = CommandService.cacheKey(userId);

        when(valueOps.get(key)).thenReturn(null);

        Account acc = Account.builder().id(10L).currentBalance(new BigDecimal("80.00")).build();
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(acc));

        List<Transaction> txs = List.of(
                Transaction.builder()
                        .type(Type.DEPOSIT)
                        .value(new BigDecimal("100.00"))
                        .dateTime(LocalDateTime.of(2025, Month.JANUARY, 2, 12, 0, 0))
                        .build(),
                Transaction.builder()
                        .type(Type.PAYMENT)
                        .value(new BigDecimal("20.00"))
                        .dateTime(LocalDateTime.of(2025, Month.JANUARY, 3, 9, 30, 0))
                        .build()
        );
        when(transactionRepository.findByAccountIdOrderByDateTimeDesc(10L)).thenReturn(txs);

        BalanceResponse resp = service.balance(userId);

        assertThat(resp.totalBalance()).isEqualByComparingTo("80.00");
        assertThat(resp.Historic()).hasSize(2);
        assertThat(resp.Historic().get(0).type()).isIn("deposit", "payment");
        assertThat(resp.Historic().get(0).value()).isNotNull();
        assertThat(resp.Historic().get(0).date())
                .matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}");

        ArgumentCaptor<CacheBalance> cacheCap = ArgumentCaptor.forClass(CacheBalance.class);
        verify(valueOps).set(eq(key), cacheCap.capture());
        CacheBalance toCache = cacheCap.getValue();

        assertThat(toCache.getTotalBalance()).isEqualByComparingTo("80.00");
        assertThat(toCache.getHistoric()).hasSize(2);

        verify(accountRepository).findByUserId(userId);
        verify(transactionRepository).findByAccountIdOrderByDateTimeDesc(10L);
    }

    @Test
    void balance_treatsNullBalance_asZero() {
        Long userId = 9L;
        String key = CommandService.cacheKey(userId);

        when(valueOps.get(key)).thenReturn(null);

        Account acc = Account.builder().id(99L).currentBalance(null).build();
        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(acc));
        when(transactionRepository.findByAccountIdOrderByDateTimeDesc(99L)).thenReturn(List.of());

        BalanceResponse resp = service.balance(userId);

        assertThat(resp.totalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(valueOps).set(eq(key), any(CacheBalance.class));
    }
}
