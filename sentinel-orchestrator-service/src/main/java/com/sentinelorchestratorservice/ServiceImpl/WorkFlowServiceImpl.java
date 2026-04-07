package com.sentinelorchestratorservice.ServiceImpl;


import com.sentinelorchestratorservice.DTO.Request.WorkFlowRequest;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowDetailsResponse;
import com.sentinelorchestratorservice.Entity.WorkFlow;
import com.sentinelorchestratorservice.Enum.StepStatus;
import com.sentinelorchestratorservice.Enum.WorkFlowStatus;
import com.sentinelorchestratorservice.Enum.WorkFlowStepName;
import com.sentinelorchestratorservice.Repository.WorkFlowRepository;
import com.sentinelorchestratorservice.Service.WorkFlowService;
import com.sentinelorchestratorservice.Service.WorkFlowServiceMethods;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    @Autowired
    private WorkFlowRepository workFlowRepository;
    @Autowired
    private WorkFlowServiceMethods workFlowServiceMethods;

    private final ObjectMapper objectMapper;
    public WorkFlowServiceImpl() {
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public WorkFlowDetailsResponse createWorkflow(WorkFlowRequest workFlowRequest) {
        log.info("createWorkflow method called for {}",workFlowRequest.toString());
        WorkFlow workFlow = new WorkFlow();
        workFlow.setWorkFlowType(workFlowRequest.getWorkflowType());
        workFlow.setUserId(workFlowRequest.getUserId());
        workFlow.setProductId(workFlowRequest.getProductId());
        workFlow.setQuantity(workFlowRequest.getQuantity());
        workFlow.setStatus(WorkFlowStatus.CREATED);
        workFlow.setCurrentStep(WorkFlowStepName.PAYMENT.name());
        try {
            workFlow.setPayload(objectMapper.writeValueAsString(workFlowRequest));
        } catch (Exception e) {
            workFlow.setPayload("{}");
        }
        workFlow.setRetries(0);
        workFlow = workFlowRepository.save(workFlow);
        workFlow.setStatus(WorkFlowStatus.IN_PROGRESS);
        return executeStep(workFlow);
    }

    public WorkFlowDetailsResponse executeStep(WorkFlow workFlow){
        log.info("executeStep method called for {}",workFlow.toString());
        StepStatus result = null;
        try {
            List<WorkFlowStepName> steps = List.of(
                    WorkFlowStepName.PAYMENT,
                    WorkFlowStepName.INVENTORY,
                    WorkFlowStepName.NOTIFICATION);
            for (int i = WorkFlowStepName.valueOf(workFlow.getCurrentStep()).number(); i < steps.size(); i++) {
                WorkFlowStepName step = steps.get(i);
                result = workFlowServiceMethods.createStep(
                        workFlow,
                        step.toString(),
                        i, workFlow.getPayload());
                log.info("Current Executing step: {},for {}", step,workFlow);
                if (result != StepStatus.SUCCESS) {
                    switch (step) {
                        case PAYMENT -> {
                            workFlow.setStatus(WorkFlowStatus.FAILED);
                            log.error("PAYMENT failed for {} and reason: {}",workFlow,step);
                            workFlow.setCurrentStep(step.toString());
                            workFlowRepository.save(workFlow);
                            return workFlowServiceMethods.getWorkFlowById(workFlow.getWorkFlowId());
                        }
                        case INVENTORY -> {
                            workFlow.setStatus(WorkFlowStatus.COMPENSATING_PAYMENT);
                            log.error("INVENTORY failed for {} and reason: {}",workFlow,step);
                            workFlow.setCurrentStep(step.toString());
                            workFlow = workFlowRepository.save(workFlow);
                            workFlowServiceMethods.compensatePayment(workFlow.getWorkFlowId());
                            return workFlowServiceMethods.getWorkFlowById(workFlow.getWorkFlowId());
                        }
                        case NOTIFICATION -> {
                            workFlow.setStatus(WorkFlowStatus.RETRY_QUERY);
                            log.error("NOTIFICATION failed for {} and reason: {}",workFlow,step);
                            workFlow.setCurrentStep(step.toString());
                            workFlowRepository.save(workFlow);
                            return workFlowServiceMethods.getWorkFlowById(workFlow.getWorkFlowId());
                        }
                    }
                }
                workFlow.setCurrentStep(step.toString());
                workFlowRepository.save(workFlow);
            }
        } catch (Exception e) {
            workFlow.setStatus(WorkFlowStatus.FAILED);
            log.error("Workflow execution failed for {} and reason: {}",workFlow,e.getMessage());
            workFlow.setLastError(e.getMessage());
            if (WorkFlowStepName.valueOf(workFlow.getCurrentStep()) == WorkFlowStepName.INVENTORY) {
                workFlow.setStatus(WorkFlowStatus.COMPENSATING_PAYMENT);
                workFlowServiceMethods.compensatePayment(workFlow.getWorkFlowId());
            }else if(WorkFlowStepName.valueOf(workFlow.getCurrentStep()) == WorkFlowStepName.NOTIFICATION) {
                workFlow.setStatus(WorkFlowStatus.RETRY_QUERY);
            }
            return workFlowServiceMethods.getWorkFlowById(workFlow.getWorkFlowId());
        }
        workFlow.setStatus(WorkFlowStatus.COMPLETED);
        workFlowRepository.save(workFlow);
        return workFlowServiceMethods.getWorkFlowById(workFlow.getWorkFlowId());
    }
    @Transactional
    public void resumePendingServices() {
        List<WorkFlow> workFlows = workFlowRepository.findByStatus(
                WorkFlowStatus.IN_PROGRESS);
        for (WorkFlow workFlow : workFlows) {
            log.info("resumePendingServices method called for {}",workFlow.toString());
            executeStep(workFlow);
        }
    }
}


