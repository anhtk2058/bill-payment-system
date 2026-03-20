package com.billpayment.repository;

import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory store for Payment records. Thread-safe.
 */
public class PaymentRepository {

    private final ConcurrentHashMap<Integer, Payment> store = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public Payment save(Payment payment) {
        if (payment.getId() == 0) {
            int newId = idSequence.getAndIncrement();
            Payment withId = Payment.builder()
                    .id(newId)
                    .amount(payment.getAmount())
                    .paymentDate(payment.getPaymentDate())
                    .state(payment.getState())
                    .billId(payment.getBillId())
                    .build();
            store.put(newId, withId);
            return withId;
        } else {
            store.put(payment.getId(), payment);
            return payment;
        }
    }

    public Optional<Payment> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Payment> findAll() {
        return store.values().stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public List<Payment> findByState(PaymentState state) {
        return store.values().stream()
                .filter(p -> p.getState() == state)
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public Optional<Payment> findByBillId(int billId) {
        return store.values().stream()
                .filter(p -> p.getBillId() == billId)
                .findFirst();
    }

    public Optional<Payment> findPendingByBillId(int billId) {
        return store.values().stream()
                .filter(p -> p.getBillId() == billId && p.getState() == PaymentState.PENDING)
                .findFirst();
    }

    public boolean deleteById(int id) {
        return store.remove(id) != null;
    }
}
