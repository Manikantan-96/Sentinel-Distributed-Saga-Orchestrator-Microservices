package com.sentinelorchestratorservice.Service;

import com.sentinelorchestratorservice.DTO.Request.WorkFlowRequest;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowDetailsResponse;
import com.sentinelorchestratorservice.Entity.WorkFlow;

public interface WorkFlowService {
    public WorkFlowDetailsResponse createWorkflow(WorkFlowRequest workFlowRequest);
    public WorkFlowDetailsResponse executeStep(WorkFlow workFlow);
    public void resumePendingServices();
}
