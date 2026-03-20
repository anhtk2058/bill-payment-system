package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.model.Payment;
import com.billpayment.util.Formatter;

import java.util.List;

/**
 * LIST_PAYMENT
 * Feature 8: Display all payment transaction history.
 */
public class ListPaymentCommand implements Command {

    @Override
    public String name() { return "LIST_PAYMENT"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        List<Payment> payments = ctx.getPaymentService().listPayments();
        if (payments.isEmpty()) {
            System.out.println("No payment records found.");
            return;
        }
        Formatter.printPaymentTable(payments);
    }
}
