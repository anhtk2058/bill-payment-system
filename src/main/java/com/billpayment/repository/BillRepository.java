package com.billpayment.repository;

import com.billpayment.model.Bill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory store for Bills. Thread-safe.
 */
public class BillRepository {

    private final ConcurrentHashMap<Integer, Bill> store = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public Bill save(Bill bill) {
        if (bill.getId() == 0) {
            // New bill — assign ID
            int newId = idSequence.getAndIncrement();
            Bill withId = Bill.builder()
                    .id(newId)
                    .type(bill.getType())
                    .amount(bill.getAmount())
                    .dueDate(bill.getDueDate())
                    .state(bill.getState())
                    .provider(bill.getProvider())
                    .build();
            store.put(newId, withId);
            return withId;
        } else {
            // Update existing
            store.put(bill.getId(), bill);
            return bill;
        }
    }

    public Optional<Bill> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Bill> findAll() {
        return store.values().stream()
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public List<Bill> findByProvider(String provider) {
        return store.values().stream()
                .filter(b -> b.getProvider().equalsIgnoreCase(provider))
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    public boolean deleteById(int id) {
        return store.remove(id) != null;
    }

    public int size() {
        return store.size();
    }

    /** Pre-seed with an explicit id (for seed data). Adjusts sequence accordingly. */
    public void saveWithId(Bill bill) {
        store.put(bill.getId(), bill);
        // Ensure next generated ID is beyond seed IDs
        if (idSequence.get() <= bill.getId()) {
            idSequence.set(bill.getId() + 1);
        }
    }
}
