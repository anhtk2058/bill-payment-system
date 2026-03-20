package com.billpayment.command;

import com.billpayment.ApplicationContext;

/**
 * Command pattern interface — each CLI command implements this.
 */
public interface Command {
    /** The CLI keyword that triggers this command (e.g. "PAY", "CASH_IN") */
    String name();

    /** Execute the command with given arguments */
    void execute(String[] args, ApplicationContext ctx);
}
