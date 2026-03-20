package com.billpayment.service;

import com.billpayment.exception.InsufficientFundsException;

import java.math.BigDecimal;

/**
 * Manages the customer's account balance.
 * Feature 1: Cash-in
 */
public class AccountService {

    private BigDecimal balance = BigDecimal.ZERO;

    public synchronized void cashIn(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cash-in amount must be positive.");
        }
        balance = balance.add(amount);
    }

    public synchronized void deduct(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive.");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        balance = balance.subtract(amount);
    }

    /** Called by Saga compensate() to reverse a deduction. */
    public synchronized void refund(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }
        balance = balance.add(amount);
    }

    public synchronized boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }
}
