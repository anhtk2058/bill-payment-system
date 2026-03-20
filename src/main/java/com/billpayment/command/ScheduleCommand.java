package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.exception.BillNotFoundException;
import com.billpayment.util.Formatter;

import java.time.LocalDate;

/**
 * SCHEDULE <billId> <dd/MM/yyyy>
 * Feature 7: Schedule a bill payment for a future date.
 */
public class ScheduleCommand implements Command {

    @Override
    public String name() { return "SCHEDULE"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 3) {
            System.out.println("Usage: SCHEDULE <billId> <dd/MM/yyyy>");
            return;
        }
        try {
            int billId = Integer.parseInt(args[1]);
            LocalDate scheduledDate = Formatter.parseDate(args[2]);
            ctx.getPaymentService().schedulePayment(billId, scheduledDate);
            System.out.println("Payment for bill id " + billId + " is scheduled on " + Formatter.formatDate(scheduledDate));
        } catch (BillNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        }
    }
}
