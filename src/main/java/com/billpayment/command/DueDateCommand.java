package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.model.Bill;
import com.billpayment.util.Formatter;

import java.util.List;

/**
 * DUE_DATE
 * Feature 5: List all NOT_PAID bills sorted by due date ASC.
 */
public class DueDateCommand implements Command {

    @Override
    public String name() { return "DUE_DATE"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        List<Bill> bills = ctx.getBillService().listNotPaidSortedByDueDate();
        if (bills.isEmpty()) {
            System.out.println("No pending bills.");
            return;
        }
        Formatter.printBillTable(bills);
    }
}
