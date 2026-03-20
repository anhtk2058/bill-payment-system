package com.billpayment.saga;

/**
 * Immutable result of a SagaStep execution.
 */
public final class StepResult {

    private final boolean success;
    private final String reason;

    private StepResult(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public static StepResult success() {
        return new StepResult(true, null);
    }

    public static StepResult failure(String reason) {
        return new StepResult(false, reason);
    }

    public boolean isSuccess() { return success; }
    public String getReason() { return reason; }
}
