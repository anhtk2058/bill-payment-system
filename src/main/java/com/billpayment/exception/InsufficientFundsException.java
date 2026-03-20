package com.billpayment.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Sorry! Not enough fund to proceed with payment.");
    }
}
