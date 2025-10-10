package com.example.desafio_back.services;

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommandServiceTest {

    AccountRepository accountRepository;
    TransactionRepository transactionRepository;

    @SuppressWarnings("unchecked")
    RedisTemplate<String, CacheBalance> redis;

    @SuppressWarnings("unchecked")
    ValueOperations<String, CacheBalance> valueOps;

    CommandService service;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        transactionRepository = mock(TransactionRepository.class);

        redis = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

         when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service = new CommandService(accountRepository, transactionRepository, redis);
    }

    @Test
    void deposit_appliesInterest_whenBalanceIsNegative_andWritesThroughCache() {
         Account acc = Account.builder().id(1L).currentBalance(new BigDecimal("-100.00")).build();
        when(accountRepository.findByUserId(99L)).thenReturn(Optional.of(acc));
        when(valueOps.get("balance:99")).thenReturn(null);
        service.deposit(99L, new BigDecimal("200.00"));

        assertThat(acc.getCurrentBalance()).isEqualByComparingTo("89.80");
        ArgumentCaptor<Transaction> txCap = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCap.capture());
        Transaction saved = txCap.getValue();
        assertThat(saved.getType()).isEqualTo(Type.DEPOSIT);
        assertThat(saved.getValue()).isEqualByComparingTo("200.00");
        assertThat(saved.getAccount()).isSameAs(acc);
        verify(accountRepository).save(acc);

        ArgumentCaptor<CacheBalance> cbCap = ArgumentCaptor.forClass(CacheBalance.class);
        verify(valueOps).set(eq("balance:99"), cbCap.capture());
        CacheBalance cached = cbCap.getValue();
        assertThat(cached.getTotalBalance()).isEqualByComparingTo("89.80");
        assertThat(cached.getHistoric()).isNotEmpty();
        ItemHistoric first = cached.getHistoric().get(0);
        assertThat(first.type()).isEqualTo("deposit");
        assertThat(first.value()).isEqualByComparingTo("200.00");
        assertThat(first.date()).matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void deposit_addsNormally_whenBalanceIsZeroOrPositive_andUpdatesExistingCache() {
        Account acc = Account.builder().id(2L).currentBalance(new BigDecimal("50.00")).build();
        when(accountRepository.findByUserId(2L)).thenReturn(Optional.of(acc));

        CacheBalance existing = new CacheBalance();
        existing.setTotalBalance(new BigDecimal("50.00"));
        existing.getHistoric().add(new ItemHistoric("payment", new BigDecimal("10.00"), "01-01-2025 09:00:00"));
        when(valueOps.get("balance:2")).thenReturn(existing);

        service.deposit(2L, new BigDecimal("25.30"));

        assertThat(acc.getCurrentBalance()).isEqualByComparingTo("75.30");
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(acc);

        ArgumentCaptor<CacheBalance> cbCap = ArgumentCaptor.forClass(CacheBalance.class);
        verify(valueOps).set(eq("balance:2"), cbCap.capture());
        CacheBalance cached = cbCap.getValue();
        assertThat(cached.getTotalBalance()).isEqualByComparingTo("75.30");
        assertThat(cached.getHistoric()).hasSize(2);
        assertThat(cached.getHistoric().get(0).type()).isEqualTo("deposit");
        assertThat(cached.getHistoric().get(0).value()).isEqualByComparingTo("25.30");
    }

    @Test
    void pay_debits_mayBecomeNegative_andWritesThroughCache() {
        Account acc = Account.builder().id(3L).currentBalance(new BigDecimal("10.00")).build();
        when(accountRepository.findByUserId(3L)).thenReturn(Optional.of(acc));
        when(valueOps.get("balance:3")).thenReturn(null);

        service.pay(3L, new BigDecimal("25.00"));

        assertThat(acc.getCurrentBalance()).isEqualByComparingTo("-15.00");
        verify(transactionRepository).save(argThat(t ->
                t.getType() == Type.PAYMENT &&
                        t.getValue().compareTo(new BigDecimal("25.00")) == 0
        ));
        verify(accountRepository).save(acc);

        ArgumentCaptor<CacheBalance> cbCap = ArgumentCaptor.forClass(CacheBalance.class);
        verify(valueOps).set(eq("balance:3"), cbCap.capture());
        CacheBalance cached = cbCap.getValue();
        assertThat(cached.getTotalBalance()).isEqualByComparingTo("-15.00");
        assertThat(cached.getHistoric()).isNotEmpty();
        assertThat(cached.getHistoric().get(0).type()).isEqualTo("payment");
    }

    @Test
    void deposit_rejectsInvalidAmount() {
        assertThatThrownBy(() -> service.deposit(1L, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.deposit(1L, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.deposit(1L, new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository);
        verify(valueOps, never()).set(anyString(), any());
    }

    @Test
    void pay_rejectsInvalidAmount() {
        assertThatThrownBy(() -> service.pay(1L, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.pay(1L, BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.pay(1L, new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository);
        verify(valueOps, never()).set(anyString(), any());
    }
}
