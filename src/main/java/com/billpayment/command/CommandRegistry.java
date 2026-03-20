package com.billpayment.command;

import com.billpayment.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping command names to Command implementations.
 * Follows Open/Closed Principle: add new commands without modifying existing code.
 */
public class CommandRegistry {

    private final Map<String, Command> registry = new HashMap<>();

    public CommandRegistry() {
        register(new CashInCommand());
        register(new ListBillCommand());
        register(new AddBillCommand());
        register(new UpdateBillCommand());
        register(new DeleteBillCommand());
        register(new PayCommand());
        register(new DueDateCommand());
        register(new ScheduleCommand());
        register(new ListPaymentCommand());
        register(new SearchBillByProviderCommand());
        register(new ExitCommand());
    }

    private void register(Command command) {
        registry.put(command.name().toUpperCase(), command);
    }

    public void dispatch(String[] args, ApplicationContext ctx) {
        if (args == null || args.length == 0) {
            printUsage();
            return;
        }
        String commandName = args[0].toUpperCase();
        Command command = registry.get(commandName);
        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            printUsage();
        } else {
            command.execute(args, ctx);
        }
    }

    private void printUsage() {
        System.out.println("Available commands:");
        System.out.println("  CASH_IN <amount>");
        System.out.println("  LIST_BILL");
        System.out.println("  ADD_BILL <TYPE> <amount> <dd/MM/yyyy> <provider>");
        System.out.println("  UPDATE_BILL <id> <TYPE> <amount> <dd/MM/yyyy> <provider>");
        System.out.println("  DELETE_BILL <id>");
        System.out.println("  PAY <billId> [billId2] ...");
        System.out.println("  DUE_DATE");
        System.out.println("  SCHEDULE <billId> <dd/MM/yyyy>");
        System.out.println("  LIST_PAYMENT");
        System.out.println("  SEARCH_BILL_BY_PROVIDER <provider>");
        System.out.println("  EXIT");
    }
}
