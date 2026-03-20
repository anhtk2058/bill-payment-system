package com.billpayment.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Orchestration-based Saga coordinator.
 *
 * Runs each SagaStep in order. If any step fails, rolls back
 * all previously completed steps in reverse order via compensate().
 *
 * This ensures atomicity for multi-bill payments:
 * either ALL bills are paid, or NONE are (all-or-nothing).
 */
public class SagaOrchestrator {

    private final List<SagaStep> steps;

    public SagaOrchestrator(List<SagaStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Saga must have at least one step.");
        }
        this.steps = new ArrayList<>(steps);
    }

    public SagaResult run() {
        List<SagaStep> completed = new ArrayList<>();

        for (SagaStep step : steps) {
            StepResult result = step.execute();

            if (result.isSuccess()) {
                completed.add(step);
            } else {
                // Compensate all previously completed steps in REVERSE order
                ListIterator<SagaStep> it = completed.listIterator(completed.size());
                while (it.hasPrevious()) {
                    it.previous().compensate();
                }
                return SagaResult.failed(result.getReason());
            }
        }

        return SagaResult.success();
    }
}
