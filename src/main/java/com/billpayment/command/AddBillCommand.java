package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.model.Bill;
import com.billpayment.model.BillType;
import com.billpayment.util.Formatter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ADD_BILL <type> <amount> <dueDate dd/MM/yyyy> <provider>
 * Feature 2: Create a new bill.
 */
public class AddBillCommand implements Command {

    @Override
    public String name() { return "ADD_BILL"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 5) {
            System.out.println("Usage: ADD_BILL <TYPE> <amount> <dd/MM/yyyy> <provider>");
            return;
        }
        try {
            BillType type = BillType.valueOf(args[1].toUpperCase());
            BigDecimal amount = new BigDecimal(args[2]);
            LocalDate dueDate = Formatter.parseDate(args[3]);
            String provider = args[4];

            Bill bill = ctx.getBillService().add(type, amount, dueDate, provider);
            System.out.println("Bill created successfully with id " + bill.getId() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }
}
