package com.billpayment.command;

import com.billpayment.ApplicationContext;
import com.billpayment.model.Bill;
import com.billpayment.util.Formatter;

import java.util.List;

/**
 * SEARCH_BILL_BY_PROVIDER <providerName>
 * Feature 2: Search bills by provider name (case-insensitive).
 */
public class SearchBillByProviderCommand implements Command {

    @Override
    public String name() { return "SEARCH_BILL_BY_PROVIDER"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        if (args.length < 2) {
            System.out.println("Usage: SEARCH_BILL_BY_PROVIDER <providerName>");
            return;
        }
        // Support provider names with spaces by joining remaining args
        String provider = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        List<Bill> bills = ctx.getBillService().searchByProvider(provider);
        if (bills.isEmpty()) {
            System.out.println("No bills found for provider: " + provider);
            return;
        }
        Formatter.printBillTable(bills);
    }
}
