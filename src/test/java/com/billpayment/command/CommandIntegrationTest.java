package com.billpayment.command;

import com.billpayment.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that verify the full CLI command pipeline:
 * ApplicationContext -> CommandRegistry -> Command -> Service -> Repository -> Output
 *
 * Uses System.out capture to assert printed output, matching the exact
 * format specified in the assignment examples.
 *
 * Note: EXIT command is not tested here because it calls System.exit(0),
 * which terminates the JVM. Its behaviour is verified via interactive shell testing.
 */
@DisplayName("Command Integration Tests")
class CommandIntegrationTest {

    private ApplicationContext ctx;
    private ByteArrayOutputStream outCapture;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        ctx = new ApplicationContext();
        outCapture = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outCapture));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        try {
            ctx.getSchedulerService().stop();
        } catch (Exception ignored) {}
    }

    private void dispatch(String... args) {
        ctx.getCommandRegistry().dispatch(args, ctx);
    }

    private String output() {
        return outCapture.toString().trim();
    }

    // Feature 1: CASH_IN

    @Test
    @DisplayName("CASH_IN: prints updated balance")
    void cashIn_printsAvailableBalance() {
        dispatch("CASH_IN", "1000000");
        assertEquals("Your available balance: 1000000", output());
    }

    @Test
    @DisplayName("CASH_IN: accumulates multiple deposits")
    void cashIn_multipleDeposits_accumulatesBalance() {
        dispatch("CASH_IN", "500000");
        dispatch("CASH_IN", "300000");
        assertTrue(output().contains("800000"));
    }

    // Feature 2: LIST_BILL

    @Test
    @DisplayName("LIST_BILL: shows seed data with correct header")
    void listBill_showsSeedDataWithHeader() {
        dispatch("LIST_BILL");
        String out = output();
        assertTrue(out.contains("Bill No."));
        assertTrue(out.contains("ELECTRIC"));
        assertTrue(out.contains("EVN HCMC"));
        assertTrue(out.contains("WATER"));
        assertTrue(out.contains("INTERNET"));
        assertTrue(out.contains("VNPT"));
    }

    // Feature 3: PAY single

    @Test
    @DisplayName("PAY: successful payment prints confirmation and new balance")
    void pay_success_printsConfirmationAndBalance() {
        dispatch("CASH_IN", "1000000");
        outCapture.reset();

        dispatch("PAY", "1");
        String out = output();
        assertTrue(out.contains("Payment has been completed for Bill with id 1."));
        assertTrue(out.contains("800000")); // 1000000 - 200000
    }

    @Test
    @DisplayName("PAY: non-existent bill prints friendly error")
    void pay_nonExistentBill_printsFriendlyError() {
        dispatch("PAY", "10");
        assertEquals("Sorry! Not found a bill with such id", output());
    }

    @Test
    @DisplayName("PAY: paying an already-PAID bill prints Sorry error")
    void pay_alreadyPaidBill_printsError() {
        dispatch("CASH_IN", "1000000");
        dispatch("PAY", "1");
        outCapture.reset();

        dispatch("PAY", "1");
        assertTrue(output().contains("Sorry!"));
    }

    // Feature 4 & 6: PAY multiple

    @Test
    @DisplayName("PAY multi: insufficient funds prints error")
    void payMultiple_insufficientFunds_printsError() {
        dispatch("CASH_IN", "1000000");
        dispatch("PAY", "1");       // balance = 800000
        outCapture.reset();

        dispatch("PAY", "2", "3");  // needs 175000+800000=975000 > 800000
        assertEquals("Sorry! Not enough fund to proceed with payment.", output());
    }

    // Feature 5: DUE_DATE

    @Test
    @DisplayName("DUE_DATE: after paying bill 1, only bills 2 & 3 shown")
    void dueDate_afterPayingOne_showsRemainingNotPaid() {
        dispatch("CASH_IN", "1000000");
        dispatch("PAY", "1");
        outCapture.reset();

        dispatch("DUE_DATE");
        String out = output();
        assertFalse(out.contains("ELECTRIC")); // bill 1 is PAID
        assertTrue(out.contains("WATER"));
        assertTrue(out.contains("INTERNET"));
    }

    // Feature 7: SCHEDULE

    @Test
    @DisplayName("SCHEDULE: prints confirmation message")
    void schedule_printsConfirmation() {
        dispatch("SCHEDULE", "2", "28/10/2020");
        assertTrue(output().contains("Payment for bill id 2 is scheduled on 28/10/2020"));
    }

    @Test
    @DisplayName("SCHEDULE: past date prints past-date warning")
    void schedule_pastDate_printsPastDateWarning() {
        dispatch("SCHEDULE", "1", "01/01/2020");
        assertTrue(output().contains("Note: Scheduled date is in the past"));
    }

    @Test
    @DisplayName("SCHEDULE: non-existent bill prints error")
    void schedule_nonExistentBill_printsError() {
        dispatch("SCHEDULE", "99", "28/10/2020");
        assertEquals("Sorry! Not found a bill with such id", output());
    }

    // Feature 8: LIST_PAYMENT

    @Test
    @DisplayName("LIST_PAYMENT: after PAY, shows PROCESSED record")
    void listPayment_afterPay_showsProcessedRecord() {
        dispatch("CASH_IN", "1000000");
        dispatch("PAY", "1");
        outCapture.reset();

        dispatch("LIST_PAYMENT");
        String out = output();
        assertTrue(out.contains("No."));
        assertTrue(out.contains("PROCESSED"));
        assertTrue(out.contains("200000"));
    }

    @Test
    @DisplayName("LIST_PAYMENT: after SCHEDULE, shows PENDING record")
    void listPayment_afterSchedule_showsPendingRecord() {
        dispatch("SCHEDULE", "2", "28/10/2020");
        outCapture.reset();

        dispatch("LIST_PAYMENT");
        String out = output();
        assertTrue(out.contains("PENDING"));
        assertTrue(out.contains("175000"));
    }

    // Feature 2: SEARCH_BILL_BY_PROVIDER

    @Test
    @DisplayName("SEARCH_BILL_BY_PROVIDER: exact name returns bill")
    void searchByProvider_exactMatch_returnsBill() {
        dispatch("SEARCH_BILL_BY_PROVIDER", "VNPT");
        String out = output();
        assertTrue(out.contains("INTERNET"));
        assertTrue(out.contains("VNPT"));
    }

    @Test
    @DisplayName("SEARCH_BILL_BY_PROVIDER: partial name returns matching bill")
    void searchByProvider_partialMatch_returnsBill() {
        dispatch("SEARCH_BILL_BY_PROVIDER", "EVN");
        String out = output();
        assertTrue(out.contains("ELECTRIC"));
        assertTrue(out.contains("EVN HCMC"));
    }

    @Test
    @DisplayName("SEARCH_BILL_BY_PROVIDER: case-insensitive match")
    void searchByProvider_caseInsensitive_returnsBill() {
        dispatch("SEARCH_BILL_BY_PROVIDER", "vnpt");
        assertTrue(output().contains("INTERNET"));
    }

    @Test
    @DisplayName("SEARCH_BILL_BY_PROVIDER: no match prints empty message")
    void searchByProvider_noMatch_returnsNoResults() {
        dispatch("SEARCH_BILL_BY_PROVIDER", "UNKNOWN_PROVIDER_XYZ");
        assertEquals("No bills found for provider: UNKNOWN_PROVIDER_XYZ", output());
    }

    // Feature 2: ADD_BILL

    @Test
    @DisplayName("ADD_BILL: invalid type prints friendly error listing valid types")
    void addBill_invalidType_printsFriendlyError() {
        dispatch("ADD_BILL", "TELECOM", "100000", "01/01/2021", "PROVIDER");
        String out = output();
        assertTrue(out.contains("Invalid bill type: TELECOM"), "Expected 'Invalid bill type: TELECOM' in: " + out);
        assertTrue(out.contains("ELECTRIC"), "Expected 'ELECTRIC' in: " + out);
    }

    @Test
    @DisplayName("ADD_BILL: valid bill creation prints success with assigned id")
    void addBill_valid_printsSuccessWithId() {
        dispatch("ADD_BILL", "ELECTRIC", "150000", "15/12/2021", "NEW PROVIDER");
        assertTrue(output().contains("Bill created successfully with id"));
    }

    // Feature 2: UPDATE_BILL

    @Test
    @DisplayName("UPDATE_BILL: cannot update PAID bill — prints Sorry error")
    void updateBill_paidBill_printsError() {
        dispatch("CASH_IN", "1000000");
        dispatch("PAY", "1");
        outCapture.reset();

        dispatch("UPDATE_BILL", "1", "ELECTRIC", "999999", "25/10/2020", "NEW PROVIDER");
        assertTrue(output().contains("Sorry!"));
    }
}
