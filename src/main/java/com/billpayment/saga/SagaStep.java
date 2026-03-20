package com.billpayment.saga;

/**
 * Represents one transactional step in a Saga.
 * If execute() fails for any step, compensate() is called
 * on all previously completed steps in reverse order.
 */
public interface SagaStep {

    /**
     * Execute this step's business logic.
     * @return StepResult indicating success or failure with a reason.
     */
    StepResult execute();

    /**
     * Compensating transaction — undo the effects of a successful execute().
     * Must be idempotent.
     */
    void compensate();
}
