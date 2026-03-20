package com.billpayment.service;

import com.billpayment.model.Payment;
import com.billpayment.model.PaymentState;
import com.billpayment.repository.PaymentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler that auto-processes PENDING payments on their scheduled date.
 * Feature 7: Automatic payment scheduling.
 *
 * Uses a daemon thread so it does not block JVM shutdown.
 */
public class SchedulerService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ScheduledExecutorService executor;

    public SchedulerService(PaymentRepository paymentRepository, PaymentService paymentService) {
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "payment-scheduler");
            t.setDaemon(true); // Does not prevent JVM shutdown
            return t;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::processDuePendingPayments, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Checks all PENDING payments and processes those whose scheduled date has arrived */
    void processDuePendingPayments() {
        LocalDate today = LocalDate.now();
        List<Payment> pending = paymentRepository.findByState(PaymentState.PENDING);
        for (Payment payment : pending) {
            if (!payment.getPaymentDate().isAfter(today)) {
                paymentService.processPayment(payment);
            }
        }
    }
}
