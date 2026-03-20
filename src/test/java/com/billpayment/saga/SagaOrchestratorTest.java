package com.billpayment.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SagaOrchestrator Tests")
class SagaOrchestratorTest {

    /** Test double: tracks execute/compensate calls */
    static class MockSagaStep implements SagaStep {
        private final boolean shouldSucceed;
        boolean executed = false;
        boolean compensated = false;

        MockSagaStep(boolean shouldSucceed) {
            this.shouldSucceed = shouldSucceed;
        }

        @Override
        public StepResult execute() {
            executed = true;
            return shouldSucceed ? StepResult.success() : StepResult.failure("Step failed intentionally");
        }

        @Override
        public void compensate() {
            compensated = true;
        }
    }

    @Test
    @DisplayName("All steps succeed → SagaResult.success, no compensations")
    void allStepsSucceed_returnsSuccess() {
        MockSagaStep step1 = new MockSagaStep(true);
        MockSagaStep step2 = new MockSagaStep(true);
        MockSagaStep step3 = new MockSagaStep(true);

        SagaOrchestrator orchestrator = new SagaOrchestrator(List.of(step1, step2, step3));
        SagaResult result = orchestrator.run();

        assertTrue(result.isSuccess());
        assertFalse(step1.compensated);
        assertFalse(step2.compensated);
        assertFalse(step3.compensated);
    }

    @Test
    @DisplayName("First step fails → SagaResult.failed, no compensations needed")
    void firstStepFails_returnsFailedNoCompensations() {
        MockSagaStep step1 = new MockSagaStep(false);
        MockSagaStep step2 = new MockSagaStep(true);

        SagaOrchestrator orchestrator = new SagaOrchestrator(List.of(step1, step2));
        SagaResult result = orchestrator.run();

        assertFalse(result.isSuccess());
        assertFalse(step1.compensated); // step1 failed so it never completed
        assertFalse(step2.executed);   // step2 never ran
    }

    @Test
    @DisplayName("Second step fails → step1 is compensated, step2 is not")
    void secondStepFails_compensatesFirstStep() {
        MockSagaStep step1 = new MockSagaStep(true);
        MockSagaStep step2 = new MockSagaStep(false);

        SagaOrchestrator orchestrator = new SagaOrchestrator(List.of(step1, step2));
        SagaResult result = orchestrator.run();

        assertFalse(result.isSuccess());
        assertTrue(step1.compensated);   // step1 completed, must be rolled back
        assertFalse(step2.compensated);  // step2 failed, nothing to roll back
    }

    @Test
    @DisplayName("Third step fails → step1 and step2 are compensated in reverse order")
    void thirdStepFails_compensatesStep2ThenStep1() {
        List<String> compensationOrder = new ArrayList<>();

        SagaStep s1 = new SagaStep() {
            public StepResult execute() { return StepResult.success(); }
            public void compensate() { compensationOrder.add("step1"); }
        };
        SagaStep s2 = new SagaStep() {
            public StepResult execute() { return StepResult.success(); }
            public void compensate() { compensationOrder.add("step2"); }
        };
        SagaStep s3 = new SagaStep() {
            public StepResult execute() { return StepResult.failure("fail"); }
            public void compensate() { compensationOrder.add("step3"); }
        };

        SagaResult result = new SagaOrchestrator(List.of(s1, s2, s3)).run();

        assertFalse(result.isSuccess());
        // Compensation must be in REVERSE order: step2 first, then step1
        assertEquals(List.of("step2", "step1"), compensationOrder);
    }

    @Test
    @DisplayName("Empty steps list throws IllegalArgumentException")
    void emptySteps_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new SagaOrchestrator(List.of()));
    }

    @Test
    @DisplayName("Failed result contains the error message")
    void failedResult_containsErrorMessage() {
        SagaStep failStep = new SagaStep() {
            public StepResult execute() { return StepResult.failure("Custom error message"); }
            public void compensate() {}
        };
        SagaResult result = new SagaOrchestrator(List.of(failStep)).run();
        assertFalse(result.isSuccess());
        assertEquals("Custom error message", result.getErrorMessage());
    }
}
