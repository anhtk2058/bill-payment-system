package com.billpayment.service;

import com.billpayment.model.Bill;
import com.billpayment.model.BillState;
import com.billpayment.model.BillType;
import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SchedulerService.processDuePendingPayments().
 *
 * We test the scheduling logic directly (calling the package-private method)
 * rather than relying on real threads + sleep, making tests fast and deterministic.
 */
@DisplayName("SchedulerService Tests")
class SchedulerServiceTest {

    private AccountService accountService;
    private BillService billService;
    private PaymentRepository paymentRepository;
    private PaymentService paymentService;
    private SchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        billService = new BillService(new BillRepository());
        paymentRepository = new PaymentRepository();
        paymentService = new PaymentService(accountService, billService, paymentRepository);
        schedulerService = new SchedulerService(paymentRepository, paymentService);
    }

    private Bill addBill(BigDecimal amount) {
        return billService.add(BillType.ELECTRIC, amount, LocalDate.now().plusDays(30), "TEST PROVIDER");
    }

    private Payment schedulePendingPayment(int billId, LocalDate date) {
        return paymentService.schedulePayment(billId, date);
    }

    @Test
    @DisplayName("PENDING payment due today should be processed automatically")
    void pendingPayment_dueToday_shouldBeProcessed() {
        accountService.cashIn(new BigDecimal("500000"));
        Bill bill = addBill(new BigDecimal("200000"));
        schedulePendingPayment(bill.getId(), LocalDate.now());

        schedulerService.processDuePendingPayments();

        assertEquals(BillState.PAID, billService.findById(bill.getId()).getState());
        assertEquals(new BigDecimal("300000"), accountService.getBalance());
    }

    @Test
    @DisplayName("PENDING payment with past due date should be processed automatically")
    void pendingPayment_dueInPast_shouldBeProcessed() {
        accountService.cashIn(new BigDecimal("500000"));
        Bill bill = addBill(new BigDecimal("175000"));
        schedulePendingPayment(bill.getId(), LocalDate.now().minusDays(5));

        schedulerService.processDuePendingPayments();

        assertEquals(BillState.PAID, billService.findById(bill.getId()).getState());
        assertEquals(new BigDecimal("325000"), accountService.getBalance());
    }

    @Test
    @DisplayName("PENDING payment due in future should NOT be processed yet")
    void pendingPayment_dueInFuture_shouldNotBeProcessed() {
        accountService.cashIn(new BigDecimal("500000"));
        Bill bill = addBill(new BigDecimal("200000"));
        schedulePendingPayment(bill.getId(), LocalDate.now().plusDays(3));

        schedulerService.processDuePendingPayments();

        // Bill should still be NOT_PAID, balance untouched
        assertEquals(BillState.NOT_PAID, billService.findById(bill.getId()).getState());
        assertEquals(new BigDecimal("500000"), accountService.getBalance());
    }

    @Test
    @DisplayName("PENDING payment with insufficient funds should remain PENDING")
    void pendingPayment_insufficientFunds_remainsPending() {
        accountService.cashIn(new BigDecimal("50000")); // not enough
        Bill bill = addBill(new BigDecimal("200000"));
        schedulePendingPayment(bill.getId(), LocalDate.now());

        schedulerService.processDuePendingPayments();

        // Bill should remain NOT_PAID
        assertEquals(BillState.NOT_PAID, billService.findById(bill.getId()).getState());
        // Balance should be unchanged
        assertEquals(new BigDecimal("50000"), accountService.getBalance());
        // Payment record state remains PENDING
        List<Payment> payments = paymentService.listPayments();
        assertEquals(1, payments.size());
        assertEquals(PaymentState.PENDING, payments.get(0).getState());
    }

    @Test
    @DisplayName("Multiple PENDING payments: only due ones should be processed")
    void multiplePayments_onlyDueOnesProcessed() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill bill1 = addBill(new BigDecimal("200000"));
        Bill bill2 = addBill(new BigDecimal("150000"));

        schedulePendingPayment(bill1.getId(), LocalDate.now());           // due today → process
        schedulePendingPayment(bill2.getId(), LocalDate.now().plusDays(7)); // future → skip

        schedulerService.processDuePendingPayments();

        assertEquals(BillState.PAID, billService.findById(bill1.getId()).getState());
        assertEquals(BillState.NOT_PAID, billService.findById(bill2.getId()).getState());
        assertEquals(new BigDecimal("800000"), accountService.getBalance());
    }

    @Test
    @DisplayName("Already PROCESSED payment should not be re-processed")
    void processedPayment_shouldNotBeReprocessed() {
        accountService.cashIn(new BigDecimal("1000000"));
        Bill bill = addBill(new BigDecimal("200000"));

        // First: direct payment (creates PROCESSED record)
        paymentService.paySingle(bill.getId());
        BigDecimal balanceAfterFirstPay = accountService.getBalance(); // 800000

        // Now simulate running the scheduler — should not double-charge
        schedulerService.processDuePendingPayments();

        // Balance must not change further
        assertEquals(balanceAfterFirstPay, accountService.getBalance());
    }
}
