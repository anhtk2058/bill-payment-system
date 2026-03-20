package com.billpayment.service;

import com.billpayment.model.Bill;
import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.saga.SagaOrchestrator;
import com.billpayment.saga.SagaResult;
import com.billpayment.saga.SagaStep;
import com.billpayment.saga.step.PayBillStep;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Orchestrates bill payment transactions using the Saga pattern.
 * Feature 3: Pay single bill
 * Feature 4 & 6: Pay multiple bills (sorted by due date, all-or-nothing)
 * Feature 8: List payment history
 */
public class PaymentService {

    private final AccountService accountService;
    private final BillService billService;
    private final PaymentRepository paymentRepository;

    public PaymentService(AccountService accountService,
                          BillService billService,
                          PaymentRepository paymentRepository) {
        this.accountService = accountService;
        this.billService = billService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Pay a single bill. Uses a Saga with one step for consistency.
     */
    public SagaResult paySingle(int billId) {
        return payMultiple(List.of(billId));
    }

    /**
     * Pay multiple bills at once.
     * Bills are sorted by due date (earliest first) before payment.
     * All-or-nothing: if any step fails, all completed steps are rolled back.
     */
    public SagaResult payMultiple(List<Integer> billIds) {
        // Fetch all bills and validate existence
        List<Bill> bills = new ArrayList<>();
        for (int id : billIds) {
            bills.add(billService.findById(id)); // throws BillNotFoundException if missing
        }

        // Sort by due date ASC (Feature 4: prioritize earliest due date)
        bills.sort(Comparator.comparing(Bill::getDueDate));

        // Build Saga steps
        List<SagaStep> steps = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Bill bill : bills) {
            steps.add(new PayBillStep(bill, accountService, billService, paymentRepository, bill.getDueDate()));
        }

        // Run Saga
        SagaOrchestrator orchestrator = new SagaOrchestrator(steps);
        return orchestrator.run();
    }

    /** Schedule a pending payment for a bill on a specific date. Feature 7 */
    public Payment schedulePayment(int billId, LocalDate scheduledDate) {
        Bill bill = billService.findById(billId); // validates existence
        return paymentRepository.save(Payment.builder()
                .amount(bill.getAmount())
                .paymentDate(scheduledDate)
                .state(PaymentState.PENDING)
                .billId(billId)
                .build());
    }

    /** Feature 8: Get all payment records sorted by id */
    public List<Payment> listPayments() {
        return paymentRepository.findAll();
    }

    /** Used by SchedulerService to process a pending payment */
    public void processPayment(Payment payment) {
        SagaResult result = paySingle(payment.getBillId());
        if (result.isSuccess()) {
            // Update payment record to PROCESSED with today's date
            Payment updated = Payment.builder()
                    .id(payment.getId())
                    .amount(payment.getAmount())
                    .paymentDate(LocalDate.now())
                    .state(PaymentState.PROCESSED)
                    .billId(payment.getBillId())
                    .build();
            paymentRepository.save(updated);
        }
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }
}
