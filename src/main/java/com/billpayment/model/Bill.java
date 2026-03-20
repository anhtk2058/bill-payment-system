package com.billpayment.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable representation of a bill.
 * Use withXxx() methods to produce updated copies.
 */
public final class Bill {

    private final int id;
    private final BillType type;
    private final BigDecimal amount;
    private final LocalDate dueDate;
    private final BillState state;
    private final String provider;

    private Bill(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.amount = builder.amount;
        this.dueDate = builder.dueDate;
        this.state = builder.state;
        this.provider = builder.provider;
    }

    public int getId() { return id; }
    public BillType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public BillState getState() { return state; }
    public String getProvider() { return provider; }

    public boolean isPayable() {
        return state.isPayable();
    }

    /** Return a copy with a new state */
    public Bill withState(BillState newState) {
        return new Builder(this).state(newState).build();
    }

    /** Return a copy with updated fields */
    public Bill withDetails(BillType type, BigDecimal amount, LocalDate dueDate, String provider) {
        return new Builder(this)
                .type(type)
                .amount(amount)
                .dueDate(dueDate)
                .provider(provider)
                .build();
    }

    @Override
    public String toString() {
        return String.format("Bill{id=%d, type=%s, amount=%s, dueDate=%s, state=%s, provider='%s'}",
                id, type, amount, dueDate, state, provider);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private BillType type;
        private BigDecimal amount;
        private LocalDate dueDate;
        private BillState state = BillState.NOT_PAID;
        private String provider;

        public Builder() {}

        // Copy constructor
        private Builder(Bill bill) {
            this.id = bill.id;
            this.type = bill.type;
            this.amount = bill.amount;
            this.dueDate = bill.dueDate;
            this.state = bill.state;
            this.provider = bill.provider;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder type(BillType type) { this.type = type; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder state(BillState state) { this.state = state; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }

        public Bill build() {
            if (type == null) throw new IllegalStateException("BillType is required");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalStateException("Amount must be positive");
            if (dueDate == null) throw new IllegalStateException("DueDate is required");
            if (provider == null || provider.isBlank()) throw new IllegalStateException("Provider is required");
            return new Bill(this);
        }
    }
}
