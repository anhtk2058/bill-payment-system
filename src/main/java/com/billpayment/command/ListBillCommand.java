package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.model.Bill;
import com.billpayment.util.Formatter;

import java.util.List;

/**
 * LIST_BILL
 * Feature 2: Display all bills.
 */
public class ListBillCommand implements Command {

    @Override
    public String name() { return "LIST_BILL"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        List<Bill> bills = ctx.getBillService().listAll();
        if (bills.isEmpty()) {
            System.out.println("No bills found.");
            return;
        }
        Formatter.printBillTable(bills);
    }
}
