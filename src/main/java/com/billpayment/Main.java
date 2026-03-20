package com.billpayment;

import java.util.Scanner;

/**
 * Entry point for the Bill Payment System CLI.
 *
 * Two modes:
 *   1. Interactive shell (no args): runs a REPL — type commands, state persists.
 *   2. Single-command mode (with args): executes one command and exits.
 *
 * Usage:
 *   java -jar bill-payment.jar              # Interactive shell
 *   java -jar bill-payment.jar CASH_IN 1000000  # Single command
 */
public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext();
        context.getSchedulerService().start();

        if (args.length == 0) {
            // Interactive shell mode — read commands from stdin
            runInteractiveShell(context);
        } else {
            // Single-command mode — execute once and exit (for scripting/testing)
            context.getCommandRegistry().dispatch(args, context);
        }
    }

    private static void runInteractiveShell(ApplicationContext context) {
        System.out.println("Bill Payment System. Type a command or EXIT to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                context.getCommandRegistry().dispatch(parts, context);
            }
        }
        context.getSchedulerService().stop();
    }
}
