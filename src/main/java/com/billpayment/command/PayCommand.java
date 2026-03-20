package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.exception.BillNotFoundException;
import com.billpayment.saga.SagaResult;
import com.billpayment.util.Formatter;

import java.util.ArrayList;
import java.util.List;

/**
 * PAY <billId> [billId2] [billId3] ...
 * Feature 3: Pay a single bill.
 * Feature 4 & 6: Pay multiple bills (sorted by due date, all-or-nothing via Saga).
 */
public class PayCommand implements Command {

    @Override
    public String name() { return "PAY"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 2) {
            System.out.println("Usage: PAY <billId> [billId2] ...");
            return;
        }

        try {
            List<Integer> ids = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                ids.add(Integer.parseInt(args[i]));
            }

            if (ids.size() == 1) {
                SagaResult result = ctx.getPaymentService().paySingle(ids.get(0));
                if (result.isSuccess()) {
                    System.out.println("Payment has been completed for Bill with id " + ids.get(0) + ".");
                    System.out.println("Your current balance is: " + Formatter.formatAmount(ctx.getAccountService().getBalance()));
                } else {
                    System.out.println(result.getErrorMessage());
                }
            } else {
                SagaResult result = ctx.getPaymentService().payMultiple(ids);
                if (result.isSuccess()) {
                    System.out.println("Payment has been completed for bills: " + ids + ".");
                    System.out.println("Your current balance is: " + Formatter.formatAmount(ctx.getAccountService().getBalance()));
                } else {
                    System.out.println(result.getErrorMessage());
                }
            }

        } catch (BillNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid bill id.");
        }
    }
}
