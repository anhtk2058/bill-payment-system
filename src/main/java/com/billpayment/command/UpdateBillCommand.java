package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.exception.BillNotFoundException;
import com.billpayment.model.BillType;
import com.billpayment.util.Formatter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * UPDATE_BILL <id> <type> <amount> <dueDate dd/MM/yyyy> <provider>
 * Feature 2: Update an existing bill.
 */
public class UpdateBillCommand implements Command {

    @Override
    public String name() { return "UPDATE_BILL"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 6) {
            System.out.println("Usage: UPDATE_BILL <id> <TYPE> <amount> <dd/MM/yyyy> <provider>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            BillType type = BillType.valueOf(args[2].toUpperCase());
            BigDecimal amount = new BigDecimal(args[3]);
            LocalDate dueDate = Formatter.parseDate(args[4]);
            String provider = args[5];
            ctx.getBillService().update(id, type, amount, dueDate, provider);
            System.out.println("Bill " + id + " updated successfully.");
        } catch (BillNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }
}
