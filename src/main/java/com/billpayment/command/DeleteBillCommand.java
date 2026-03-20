package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.exception.BillNotFoundException;

/**
 * DELETE_BILL <id>
 * Feature 2: Delete a bill by ID.
 */
public class DeleteBillCommand implements Command {

    @Override
    public String name() { return "DELETE_BILL"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 2) {
            System.out.println("Usage: DELETE_BILL <id>");
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            ctx.getBillService().delete(id);
            System.out.println("Bill " + id + " deleted successfully.");
        } catch (BillNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid id: " + args[1]);
        }
    }
}
