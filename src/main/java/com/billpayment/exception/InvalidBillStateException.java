package com.billpayment.exception;

public class InvalidBillStateException extends RuntimeException {
    public InvalidBillStateException(int billId) {
        super("Sorry! Bill with id " + billId + " is already paid.");
    }

    public InvalidBillStateException(int billId, String message) {
        super("Sorry! " + message);
    }
}
