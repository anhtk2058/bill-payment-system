package com.billpayment.util;

import com.billpayment.model.Bill;
import com.billpayment.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Utility class for formatting output tables and parsing date/amount values.
 * Ensures consistent output format matching the assignment's specification.
 */
public final class Formatter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatter() {} // Utility class — no instantiation

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected dd/MM/yyyy, got: " + dateStr);
        }
    }

    public static String formatAmount(BigDecimal amount) {
        // Show as integer if no decimal part, otherwise show decimals
        if (amount.stripTrailingZeros().scale() <= 0) {
            return amount.toBigInteger().toString();
        }
        return amount.toPlainString();
    }

    /**
     * Print bills in the table format specified by the assignment.
     *
     * Bill No.  Type       Amount   Due Date    State     PROVIDER
     * 1.        ELECTRIC   200000   25/10/2020  NOT_PAID  EVN HCMC
     */
    public static void printBillTable(List<Bill> bills) {
        System.out.printf("%-10s%-12s%-10s%-12s%-12s%s%n",
                "Bill No.", "Type", "Amount", "Due Date", "State", "PROVIDER");
        int index = 1;
        for (Bill bill : bills) {
            System.out.printf("%-10s%-12s%-10s%-12s%-12s%s%n",
                    index + ".",
                    bill.getType(),
                    formatAmount(bill.getAmount()),
                    formatDate(bill.getDueDate()),
                    bill.getState(),
                    bill.getProvider());
            index++;
        }
    }

    /**
     * Print payments in the table format specified by the assignment.
     *
     * No.  Amount   Payment Date  State      Bill Id
     * 1.   200000   25/10/2020    PROCESSED  1
     */
    public static void printPaymentTable(List<Payment> payments) {
        System.out.printf("%-5s%-10s%-14s%-12s%s%n",
                "No.", "Amount", "Payment Date", "State", "Bill Id");
        int index = 1;
        for (Payment payment : payments) {
            System.out.printf("%-5s%-10s%-14s%-12s%d%n",
                    index + ".",
                    formatAmount(payment.getAmount()),
                    formatDate(payment.getPaymentDate()),
                    payment.getState(),
                    payment.getBillId());
            index++;
        }
    }
}
