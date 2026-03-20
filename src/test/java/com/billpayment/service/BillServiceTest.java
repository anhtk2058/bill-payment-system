package com.billpayment.service;

import com.billpayment.exception.BillNotFoundException;
import com.billpayment.model.Bill;
import com.billpayment.model.BillState;
import com.billpayment.model.BillType;
import com.billpayment.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillService Tests")
class BillServiceTest {

    private BillService billService;

    @BeforeEach
    void setUp() {
        billService = new BillService(new BillRepository());
    }

    private Bill addSampleBill(String provider, LocalDate dueDate) {
        return billService.add(BillType.ELECTRIC, new BigDecimal("100000"), dueDate, provider);
    }

    @Test
    @DisplayName("add: creates bill with NOT_PAID state and auto-assigned id")
    void add_createsNewBill() {
        Bill bill = addSampleBill("EVN HCMC", LocalDate.of(2020, 10, 25));
        assertTrue(bill.getId() > 0);
        assertEquals(BillState.NOT_PAID, bill.getState());
        assertEquals("EVN HCMC", bill.getProvider());
    }

    @Test
    @DisplayName("findById: returns correct bill")
    void findById_returnsCorrectBill() {
        Bill created = addSampleBill("VNPT", LocalDate.of(2020, 11, 30));
        Bill found = billService.findById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("VNPT", found.getProvider());
    }

    @Test
    @DisplayName("findById: throws BillNotFoundException for unknown id")
    void findById_unknownId_throwsBillNotFoundException() {
        assertThrows(BillNotFoundException.class, () -> billService.findById(999));
    }

    @Test
    @DisplayName("listAll: returns all bills sorted by id")
    void listAll_returnsSortedById() {
        addSampleBill("Provider A", LocalDate.of(2020, 12, 1));
        addSampleBill("Provider B", LocalDate.of(2020, 11, 1));
        addSampleBill("Provider C", LocalDate.of(2020, 10, 1));
        List<Bill> all = billService.listAll();
        assertEquals(3, all.size());
        assertTrue(all.get(0).getId() < all.get(1).getId());
    }

    @Test
    @DisplayName("listNotPaidSortedByDueDate: returns only NOT_PAID bills sorted by due date ASC")
    void listNotPaidSortedByDueDate_sortsCorrectly() {
        Bill b1 = billService.add(BillType.ELECTRIC, new BigDecimal("100"), LocalDate.of(2020, 11, 30), "A");
        Bill b2 = billService.add(BillType.WATER, new BigDecimal("200"), LocalDate.of(2020, 10, 25), "B");
        Bill b3 = billService.add(BillType.INTERNET, new BigDecimal("300"), LocalDate.of(2020, 10, 30), "C");
        billService.markAsPaid(b1.getId());

        List<Bill> overdue = billService.listNotPaidSortedByDueDate();
        assertEquals(2, overdue.size());
        assertEquals(b2.getId(), overdue.get(0).getId()); // earliest due date first
        assertEquals(b3.getId(), overdue.get(1).getId());
    }

    @Test
    @DisplayName("searchByProvider: case-insensitive match")
    void searchByProvider_caseInsensitive() {
        addSampleBill("EVN HCMC", LocalDate.of(2020, 10, 25));
        List<Bill> found = billService.searchByProvider("evn hcmc");
        assertEquals(1, found.size());
    }

    @Test
    @DisplayName("searchByProvider: returns empty list for unknown provider")
    void searchByProvider_unknownProvider_returnsEmpty() {
        addSampleBill("EVN HCMC", LocalDate.of(2020, 10, 25));
        List<Bill> found = billService.searchByProvider("UNKNOWN");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("update: updates bill fields")
    void update_updatesFields() {
        Bill bill = addSampleBill("OLD PROVIDER", LocalDate.of(2020, 10, 25));
        billService.update(bill.getId(), BillType.WATER, new BigDecimal("999"), LocalDate.of(2020, 12, 31), "NEW PROVIDER");
        Bill updated = billService.findById(bill.getId());
        assertEquals("NEW PROVIDER", updated.getProvider());
        assertEquals(BillType.WATER, updated.getType());
        assertEquals(new BigDecimal("999"), updated.getAmount());
    }

    @Test
    @DisplayName("delete: removes bill successfully")
    void delete_removedBillNotFound() {
        Bill bill = addSampleBill("SAVACO", LocalDate.of(2020, 10, 30));
        billService.delete(bill.getId());
        assertThrows(BillNotFoundException.class, () -> billService.findById(bill.getId()));
    }

    @Test
    @DisplayName("markAsPaid: changes state to PAID")
    void markAsPaid_changesStateToPaid() {
        Bill bill = addSampleBill("EVN", LocalDate.of(2020, 10, 25));
        billService.markAsPaid(bill.getId());
        assertEquals(BillState.PAID, billService.findById(bill.getId()).getState());
    }

    @Test
    @DisplayName("markAsUnpaid: changes state back to NOT_PAID (Saga compensation)")
    void markAsUnpaid_revertsToNotPaid() {
        Bill bill = addSampleBill("EVN", LocalDate.of(2020, 10, 25));
        billService.markAsPaid(bill.getId());
        billService.markAsUnpaid(bill.getId());
        assertEquals(BillState.NOT_PAID, billService.findById(bill.getId()).getState());
    }
}
