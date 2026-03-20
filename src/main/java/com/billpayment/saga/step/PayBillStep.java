package com.billpayment.saga.step;

import com.billpayment.model.Bill;
import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.saga.SagaStep;
import com.billpayment.saga.StepResult;
import com.billpayment.service.AccountService;
import com.billpayment.service.BillService;

import java.time.LocalDate;

/**
 * One step in the multi-bill payment Saga.
 * execute()    → deduct balance + mark bill PAID + save payment record
 * compensate() → refund balance + mark bill NOT_PAID + remove payment record
 */
public class PayBillStep implements SagaStep {

    private final Bill bill;
    private final AccountService accountService;
    private final BillService billService;
    private final PaymentRepository paymentRepository;
    private final LocalDate paymentDate;

    private Payment createdPayment; // tracked for compensation

    public PayBillStep(Bill bill,
                       AccountService accountService,
                       BillService billService,
                       PaymentRepository paymentRepository,
                       LocalDate paymentDate) {
        this.bill = bill;
        this.accountService = accountService;
        this.billService = billService;
        this.paymentRepository = paymentRepository;
        this.paymentDate = paymentDate;
    }

    @Override
    public StepResult execute() {
        // Guard: bill must be payable
        if (!bill.isPayable()) {
            return StepResult.failure(
                "Bill " + bill.getId() + " is already paid.");
        }

        // Guard: check funds before deducting
        if (!accountService.hasSufficientFunds(bill.getAmount())) {
            return StepResult.failure("Sorry! Not enough fund to proceed with payment.");
        }

        // Execute the transaction
        accountService.deduct(bill.getAmount());
        billService.markAsPaid(bill.getId());

        createdPayment = paymentRepository.save(Payment.builder()
                .amount(bill.getAmount())
                .paymentDate(paymentDate)
                .state(PaymentState.PROCESSED)
                .billId(bill.getId())
                .build());

        return StepResult.success();
    }

    @Override
    public void compensate() {
        // Reverse: refund the balance
        accountService.refund(bill.getAmount());

        // Reverse: reset bill state
        billService.markAsUnpaid(bill.getId());

        // Reverse: remove payment record
        if (createdPayment != null) {
            paymentRepository.deleteById(createdPayment.getId());
            createdPayment = null;
        }
    }

    public Bill getBill() { return bill; }
}
