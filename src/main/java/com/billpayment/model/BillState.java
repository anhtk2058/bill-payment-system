package com.billpayment.model;

public enum BillState {
    NOT_PAID,
    PAID;

    public boolean isPayable() {
        return this == NOT_PAID;
    }
}
