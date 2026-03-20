package com.billpayment.service;

import com.billpayment.exception.BillNotFoundException;
import com.billpayment.model.Bill;
import com.billpayment.model.BillState;
import com.billpayment.model.BillType;
import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.saga.SagaResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    private AccountService accountService;
    private BillService billService;
    private PaymentRepository paymentRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        billService = new BillService(new BillRepository());
        paymentRepository = new PaymentRepository();
        paymentService = new PaymentService(accountService, billService, paymentRepository);
    }

    private Bill addBill(BigDecimal amount, LocalDate dueDate) {
        return billService.add(BillType.ELECTRIC, amount, dueDate, "TEST PROVIDER");
    }

    @Test
    @DisplayName("paySingle: successful payment marks bill as PAID and deducts balance")
    void paySingle_success_deductsBalanceAndMarksPaid() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill bill = addBill(new BigDecimal("200000"), LocalDate.of(2020, 10, 25));

        SagaResult result = paymentService.paySingle(bill.getId());

        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("800000"), accountService.getBalance());
        assertEquals(BillState.PAID, billService.findById(bill.getId()).getState());
    }

    @Test
    @DisplayName("paySingle: creates a PROCESSED payment record")
    void paySingle_success_createsProcessedPaymentRecord() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill bill = addBill(new BigDecimal("200000"), LocalDate.of(2020, 10, 25));
        paymentService.paySingle(bill.getId());

        List<Payment> payments = paymentService.listPayments();
        assertEquals(1, payments.size());
        assertEquals(PaymentState.PROCESSED, payments.get(0).getState());
        assertEquals(bill.getId(), payments.get(0).getBillId());
    }

    @Test
    @DisplayName("paySingle: insufficient funds returns failed SagaResult, balance unchanged")
    void paySingle_insufficientFunds_failsAndBalanceUnchanged() {
        accountService.cashIn(new BigDecimal("100000"));
        Bill bill = addBill(new BigDecimal("200000"), LocalDate.of(2020, 10, 25));

        SagaResult result = paymentService.paySingle(bill.getId());

        assertFalse(result.isSuccess());
        assertEquals(new BigDecimal("100000"), accountService.getBalance());
        assertEquals(BillState.NOT_PAID, billService.findById(bill.getId()).getState());
    }

    @Test
    @DisplayName("paySingle: bill not found throws BillNotFoundException")
    void paySingle_billNotFound_throwsException() {
        assertThrows(BillNotFoundException.class, () -> paymentService.paySingle(999));
    }

    @Test
    @DisplayName("payMultiple: all bills paid successfully with balance deducted")
    void payMultiple_success_allBillsPaid() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill b1 = addBill(new BigDecimal("200000"), LocalDate.of(2020, 10, 25));
        Bill b2 = addBill(new BigDecimal("175000"), LocalDate.of(2020, 10, 30));

        SagaResult result = paymentService.payMultiple(List.of(b1.getId(), b2.getId()));

        assertTrue(result.isSuccess());
        assertEquals(BillState.PAID, billService.findById(b1.getId()).getState());
        assertEquals(BillState.PAID, billService.findById(b2.getId()).getState());
        assertEquals(new BigDecimal("625000"), accountService.getBalance());
    }

    @Test
    @DisplayName("payMultiple: insufficient funds for 2nd bill — Saga rolls back 1st bill (all-or-nothing)")
    void payMultiple_insufficientFundsOnSecond_rollsBackFirst() {
        accountService.cashIn(new BigDecimal("250000")); // enough for b1 but not b1+b2
        Bill b1 = addBill(new BigDecimal("200000"), LocalDate.of(2020, 10, 25));
        Bill b2 = addBill(new BigDecimal("800000"), LocalDate.of(2020, 11, 30));

        SagaResult result = paymentService.payMultiple(List.of(b1.getId(), b2.getId()));

        assertFalse(result.isSuccess());
        // Saga should have rolled back b1 back to NOT_PAID
        assertEquals(BillState.NOT_PAID, billService.findById(b1.getId()).getState());
        assertEquals(BillState.NOT_PAID, billService.findById(b2.getId()).getState());
        // Balance should be fully restored
        assertEquals(new BigDecimal("250000"), accountService.getBalance());
        // PENDING records are created for both bills (per spec: PAY failure creates scheduled records)
        List<Payment> payments = paymentService.listPayments();
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getState() == PaymentState.PENDING));
    }

    @Test
    @DisplayName("payMultiple: bills are sorted by due date before payment (earliest first)")
    void payMultiple_sortsByDueDate() {
        accountService.cashIn(new BigDecimal("1000000"));
        // Add bills in non-date order
        Bill later  = addBill(new BigDecimal("100000"), LocalDate.of(2020, 12, 1));
        Bill earlier = addBill(new BigDecimal("100000"), LocalDate.of(2020, 10, 1));

        // Pay both — should succeed regardless of order
        SagaResult result = paymentService.payMultiple(List.of(later.getId(), earlier.getId()));
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("schedulePayment: creates PENDING payment record")
    void schedulePayment_createsPendingRecord() {
        Bill bill = addBill(new BigDecimal("175000"), LocalDate.of(2020, 10, 30));
        LocalDate scheduled = LocalDate.of(2020, 10, 28);

        Payment payment = paymentService.schedulePayment(bill.getId(), scheduled);

        assertEquals(PaymentState.PENDING, payment.getState());
        assertEquals(scheduled, payment.getPaymentDate());
        assertEquals(bill.getId(), payment.getBillId());
    }

    @Test
    @DisplayName("listPayments: returns all payment records sorted by id")
    void listPayments_returnsSortedById() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill b1 = addBill(new BigDecimal("100000"), LocalDate.of(2020, 10, 25));
        Bill b2 = addBill(new BigDecimal("100000"), LocalDate.of(2020, 10, 30));
        paymentService.paySingle(b1.getId());
        paymentService.paySingle(b2.getId());

        List<Payment> payments = paymentService.listPayments();
        assertEquals(2, payments.size());
        assertTrue(payments.get(0).getId() < payments.get(1).getId());
    }
}
