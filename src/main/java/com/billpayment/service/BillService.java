package com.billpayment.service;

import com.billpayment.exception.BillNotFoundException;
import com.billpayment.exception.InvalidBillStateException;
import com.billpayment.model.Bill;
import com.billpayment.model.BillState;
import com.billpayment.model.BillType;
import com.billpayment.repository.BillRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for bill management.
 * Feature 2: CRUD + search
 * Feature 5: DUE_DATE — list overdue bills sorted by due date
 */
public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill add(BillType type, BigDecimal amount, LocalDate dueDate, String provider) {
        Bill newBill = Bill.builder()
                .type(type)
                .amount(amount)
                .dueDate(dueDate)
                .provider(provider)
                .state(BillState.NOT_PAID)
                .build();
        return billRepository.save(newBill);
    }

    public Bill findById(int id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException(id));
    }

    public List<Bill> listAll() {
        return billRepository.findAll();
    }

    /** Feature 5: NOT_PAID bills sorted by due date ASC */
    public List<Bill> listNotPaidSortedByDueDate() {
        return billRepository.findAll().stream()
                .filter(b -> b.getState() == BillState.NOT_PAID)
                .sorted(Comparator.comparing(Bill::getDueDate))
                .collect(Collectors.toList());
    }

    /** Feature 2: Search by provider name (case-insensitive) */
    public List<Bill> searchByProvider(String provider) {
        return billRepository.findByProvider(provider);
    }

    public Bill update(int id, BillType type, BigDecimal amount, LocalDate dueDate, String provider) {
        Bill existing = findById(id);
        Bill updated = existing.withDetails(type, amount, dueDate, provider);
        return billRepository.save(updated);
    }

    public void delete(int id) {
        Bill existing = findById(id); // Throws if not found
        billRepository.deleteById(existing.getId());
    }

    /** Called by Saga execute() — marks a bill as PAID */
    public void markAsPaid(int id) {
        Bill bill = findById(id);
        if (!bill.isPayable()) {
            throw new InvalidBillStateException(id);
        }
        billRepository.save(bill.withState(BillState.PAID));
    }

    /** Called by Saga compensate() — reverts bill back to NOT_PAID */
    public void markAsUnpaid(int id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new BillNotFoundException(id));
        billRepository.save(bill.withState(BillState.NOT_PAID));
    }
}
