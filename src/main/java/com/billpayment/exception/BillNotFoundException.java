package com.billpayment.exception;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(int billId) {
        super("Sorry! Not found a bill with such id");
    }
}
