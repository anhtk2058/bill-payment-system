package com.billpayment.service;

import com.billpayment.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountService Tests")
class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
    }

    @Test
    @DisplayName("Initial balance should be zero")
    void initialBalanceShouldBeZero() {
        assertEquals(BigDecimal.ZERO, accountService.getBalance());
    }

    @Test
    @DisplayName("CASH_IN: valid positive amount increases balance")
    void cashIn_validAmount_increasesBalance() {
        accountService.cashIn(new BigDecimal("500000"));
        assertEquals(new BigDecimal("500000"), accountService.getBalance());
    }

    @Test
    @DisplayName("CASH_IN: multiple deposits accumulate correctly")
    void cashIn_multipleDeposits_accumulate() {
        accountService.cashIn(new BigDecimal("300000"));
        accountService.cashIn(new BigDecimal("200000"));
        assertEquals(new BigDecimal("500000"), accountService.getBalance());
    }

    @Test
    @DisplayName("CASH_IN: zero amount throws IllegalArgumentException")
    void cashIn_zeroAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.cashIn(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("CASH_IN: negative amount throws IllegalArgumentException")
    void cashIn_negativeAmount_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.cashIn(new BigDecimal("-100")));
    }

    @Test
    @DisplayName("deduct: sufficient balance reduces balance correctly")
    void deduct_sufficientBalance_reducesBalance() {
        accountService.cashIn(new BigDecimal("1000000"));
        accountService.deduct(new BigDecimal("200000"));
        assertEquals(new BigDecimal("800000"), accountService.getBalance());
    }

    @Test
    @DisplayName("deduct: insufficient balance throws InsufficientFundsException")
    void deduct_insufficientBalance_throwsException() {
        accountService.cashIn(new BigDecimal("100000"));
        assertThrows(InsufficientFundsException.class,
                () -> accountService.deduct(new BigDecimal("200000")));
    }

    @Test
    @DisplayName("deduct: insufficient balance does not change balance")
    void deduct_insufficientBalance_balanceUnchanged() {
        accountService.cashIn(new BigDecimal("100000"));
        try {
            accountService.deduct(new BigDecimal("200000"));
        } catch (InsufficientFundsException ignored) {}
        assertEquals(new BigDecimal("100000"), accountService.getBalance());
    }

    @Test
    @DisplayName("deduct: exact balance reduces to zero")
    void deduct_exactBalance_reducesToZero() {
        accountService.cashIn(new BigDecimal("500000"));
        accountService.deduct(new BigDecimal("500000"));
        assertEquals(BigDecimal.ZERO, accountService.getBalance());
    }

    @Test
    @DisplayName("refund: adds amount back to balance (Saga compensation)")
    void refund_addsAmountBack() {
        accountService.cashIn(new BigDecimal("1000000"));
        accountService.deduct(new BigDecimal("200000"));
        accountService.refund(new BigDecimal("200000"));
        assertEquals(new BigDecimal("1000000"), accountService.getBalance());
    }

    @Test
    @DisplayName("hasSufficientFunds: returns true when balance >= amount")
    void hasSufficientFunds_balanceSufficient_returnsTrue() {
        accountService.cashIn(new BigDecimal("500000"));
        assertTrue(accountService.hasSufficientFunds(new BigDecimal("500000")));
        assertTrue(accountService.hasSufficientFunds(new BigDecimal("300000")));
    }

    @Test
    @DisplayName("hasSufficientFunds: returns false when balance < amount")
    void hasSufficientFunds_balanceInsufficient_returnsFalse() {
        accountService.cashIn(new BigDecimal("100000"));
        assertFalse(accountService.hasSufficientFunds(new BigDecimal("200000")));
    }
}
