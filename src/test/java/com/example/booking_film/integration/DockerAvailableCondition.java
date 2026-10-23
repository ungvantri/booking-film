package com.example.booking_film.integration;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ConditionEvaluationResult.enabled("Docker daemon is available");
            }
        } catch (Throwable ignored) {
        }
        return ConditionEvaluationResult.disabled(
                "Docker daemon is not running. Please start Docker Desktop to run Testcontainers integration tests."
        );
    }
}
