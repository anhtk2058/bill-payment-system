package com.billpayment.command;

import com.billpayment.ApplicationContext;

/**
 * EXIT
 * Gracefully stops the scheduler and exits the application.
 */
public class ExitCommand implements Command {

    @Override
    public String name() { return "EXIT"; }

    @Override
    public void execute(String[] args, ApplicationContext ctx) {
        System.out.println("Good bye!");
        ctx.getSchedulerService().stop();
        System.exit(0);
    }
}
