package com.sentinelorchestratorservice.Service;

import com.sentinelorchestratorservice.DTO.Response.WorkFlowDetailsResponse;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowResponse;
import com.sentinelorchestratorservice.Entity.WorkFlow;
import com.sentinelorchestratorservice.Enum.StepStatus;

import java.util.List;

public interface WorkFlowServiceMethods {
    public WorkFlowDetailsResponse getWorkFlowById(long workflowId);
    public List<WorkFlowResponse> ListOfWorkFlows(String status);
    public String RetryWorkflow(long workflowId);
    public String compensatePayment(Long workflowId);
    public String compensateInventory(Long workflowId);
    public StepStatus createStep(WorkFlow workflow, String stepName, int stepOrder, String payload);
    String compensateInventoryManually(long workFlowId);
    String compensatePaymentManually(long workFlowId);
}
