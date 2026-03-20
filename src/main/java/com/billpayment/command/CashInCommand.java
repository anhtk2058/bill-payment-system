package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.util.Formatter;

import java.math.BigDecimal;

/**
 * CASH_IN <amount>
 * Feature 1: Add funds to the account.
 */
public class CashInCommand implements Command {

    @Override
    public String name() { return "CASH_IN"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 2) {
            System.out.println("Usage: CASH_IN <amount>");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(args[1]);
            ctx.getAccountService().cashIn(amount);
            System.out.println("Your available balance: " + Formatter.formatAmount(ctx.getAccountService().getBalance()));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount: " + args[1]);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
