package com.billpayment;

import com.billpayment.command.CommandRegistry;
import com.billpayment.model.BillState;
import com.billpayment.model.BillType;
import com.billpayment.model.Bill;
import com.billpayment.repository.BillRepository;
import com.billpayment.repository.PaymentRepository;
import com.billpayment.service.AccountService;
import com.billpayment.service.BillService;
import com.billpayment.service.PaymentService;
import com.billpayment.service.SchedulerService;
import com.billpayment.util.Formatter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Manual dependency injection container.
 * Wires all components together and loads seed data on startup.
 */
public class ApplicationContext {

    private final AccountService accountService;
    private final BillRepository billRepository;
    private final BillService billService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SchedulerService schedulerService;
    private final CommandRegistry commandRegistry;

    public ApplicationContext() {
        // Wire dependencies bottom-up
        this.accountService = new AccountService();
        this.billRepository = new BillRepository();
        this.billService = new BillService(billRepository);
        this.paymentRepository = new PaymentRepository();
        this.paymentService = new PaymentService(accountService, billService, paymentRepository);
        this.schedulerService = new SchedulerService(paymentRepository, paymentService);
        this.commandRegistry = new CommandRegistry();

        // Load seed data
        loadSeedData();
    }

    /**
     * Seeds the system with 3 initial bills matching the assignment's example output.
     */
    private void loadSeedData() {
        billRepository.saveWithId(Bill.builder()
                .id(1)
                .type(BillType.ELECTRIC)
                .amount(new BigDecimal("200000"))
                .dueDate(Formatter.parseDate("25/10/2020"))
                .state(BillState.NOT_PAID)
                .provider("EVN HCMC")
                .build());

        billRepository.saveWithId(Bill.builder()
                .id(2)
                .type(BillType.WATER)
                .amount(new BigDecimal("175000"))
                .dueDate(Formatter.parseDate("30/10/2020"))
                .state(BillState.NOT_PAID)
                .provider("SAVACO HCMC")
                .build());

        billRepository.saveWithId(Bill.builder()
                .id(3)
                .type(BillType.INTERNET)
                .amount(new BigDecimal("800000"))
                .dueDate(Formatter.parseDate("30/11/2020"))
                .state(BillState.NOT_PAID)
                .provider("VNPT")
                .build());
    }

    public AccountService getAccountService() { return accountService; }
    public BillService getBillService() { return billService; }
    public PaymentService getPaymentService() { return paymentService; }
    public SchedulerService getSchedulerService() { return schedulerService; }
    public CommandRegistry getCommandRegistry() { return commandRegistry; }
}
