package com.sentinelorchestratorservice.Enum;

public enum WorkFlowStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    RETRY_QUERY,
    COMPENSATING_PAYMENT,
    COMPENSATING_INVENTORY,
    COMPENSATED_INVENTORY,
    COMPENSATED_PAYMENT
}
