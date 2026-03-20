package com.billpayment.exception;

public class InvalidBillStateException extends RuntimeException {
    public InvalidBillStateException(int billId) {
        super("Bill with id " + billId + " is already paid or not in a payable state.");
    }
}
