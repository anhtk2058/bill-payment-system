package com.billpayment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a payment transaction (either processed or scheduled/pending).
 */
public final class Payment {

    private final int id;
    private final BigDecimal amount;
    private final LocalDate paymentDate;
    private final PaymentState state;
    private final int billId;

    private Payment(Builder builder) {
        this.id = builder.id;
        this.amount = builder.amount;
        this.paymentDate = builder.paymentDate;
        this.state = builder.state;
        this.billId = builder.billId;
    }

    public int getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public PaymentState getState() { return state; }
    public int getBillId() { return billId; }

    /** Return a copy with a new state */
    public Payment withState(PaymentState newState) {
        return new Builder(this).state(newState).build();
    }

    @Override
    public String toString() {
        return String.format("Payment{id=%d, amount=%s, paymentDate=%s, state=%s, billId=%d}",
                id, amount, paymentDate, state, billId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private BigDecimal amount;
        private LocalDate paymentDate;
        private PaymentState state = PaymentState.PENDING;
        private int billId;

        public Builder() {}

        private Builder(Payment payment) {
            this.id = payment.id;
            this.amount = payment.amount;
            this.paymentDate = payment.paymentDate;
            this.state = payment.state;
            this.billId = payment.billId;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder paymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; return this; }
        public Builder state(PaymentState state) { this.state = state; return this; }
        public Builder billId(int billId) { this.billId = billId; return this; }

        public Payment build() {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalStateException("Amount must be positive");
            if (paymentDate == null) throw new IllegalStateException("PaymentDate is required");
            return new Payment(this);
        }
    }
}
