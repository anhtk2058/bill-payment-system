package com.billpayment.saga;

/**
 * Immutable result of a full Saga run.
 */
public final class SagaResult {

    private final boolean success;
    private final String errorMessage;

    private SagaResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static SagaResult success() {
        return new SagaResult(true, null);
    }

    public static SagaResult failed(String message) {
        return new SagaResult(false, message);
    }

    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
