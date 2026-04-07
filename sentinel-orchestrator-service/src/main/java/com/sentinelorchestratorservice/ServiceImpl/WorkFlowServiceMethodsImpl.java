package com.sentinelorchestratorservice.ServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sentinelorchestratorservice.Controller.GlobalExceptionHandler;
import com.sentinelorchestratorservice.DTO.Request.ServiceRequest;
import com.sentinelorchestratorservice.DTO.Response.ServerResponse;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowDetailsResponse;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowResponse;
import com.sentinelorchestratorservice.Entity.WorkFlow;
import com.sentinelorchestratorservice.Entity.WorkFlowStep;
import com.sentinelorchestratorservice.Enum.StepStatus;
import com.sentinelorchestratorservice.Enum.WorkFlowStatus;
import com.sentinelorchestratorservice.Enum.WorkFlowStepName;
import com.sentinelorchestratorservice.FeignClient.InventoryService;
import com.sentinelorchestratorservice.FeignClient.MessageService;
import com.sentinelorchestratorservice.FeignClient.PaymentService;
import com.sentinelorchestratorservice.Repository.WorkFlowRepository;
import com.sentinelorchestratorservice.Repository.WorkFlowStepRepository;
import com.sentinelorchestratorservice.Service.WorkFlowServiceMethods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WorkFlowServiceMethodsImpl implements WorkFlowServiceMethods {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private WorkFlowRepository workFlowRepository;
    @Autowired
    private WorkFlowStepRepository workFlowStepRepository;

    private final ObjectMapper objectMapper;

    public WorkFlowServiceMethodsImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    public WorkFlowDetailsResponse getWorkFlowById(long workflowId) {
        log.info("getWorkFlowById method called for {}",workflowId);
        WorkFlow workFlow=workFlowRepository.findById(workflowId).orElseThrow(
                                      ()->new GlobalExceptionHandler.ResourceNotFoundException("Workflow not found"));
        List<WorkFlowStep> steps=workFlowStepRepository.findByWorkFlowIdOrderByStepOrderAsc(workflowId);
        WorkFlowDetailsResponse response = new WorkFlowDetailsResponse();
        response.setWorkFlowId(workFlow.getWorkFlowId());
        response.setWorkFlowType(workFlow.getWorkFlowType());
        response.setStatus(workFlow.getStatus().toString());
        response.setCurrentStep(workFlow.getCurrentStep());
        response.setPayload(workFlow.getPayload());
        response.setLastError(workFlow.getLastError());
        response.setCreatedAt(workFlow.getCreatedAt());
        response.setUpdatedAt(workFlow.getUpdatedAt());
        response.setSteps(steps);
        return response;
    }


    public List<WorkFlowResponse> ListOfWorkFlows(String status){
        log.info("ListOfWorkFlows method called for {}",status);
        List<WorkFlow> workFlows;
        if(status==null ||status.isBlank()){
            workFlows=workFlowRepository.findAll();
        }else{
            workFlows=workFlowRepository.findByStatus(WorkFlowStatus.valueOf(status.toUpperCase()));
        }
        List<WorkFlowResponse> responses=new ArrayList<>();
        for(WorkFlow workFlow:workFlows){
            responses.add(workFlowToWorkFlowResponse(workFlow));
        }
        return responses;
    }
    @Transactional
    public String RetryWorkflow(long workflowId){
        log.info("RetryWorkflow method called for {}",workflowId);
        WorkFlow workFlow=workFlowRepository.findById(workflowId).orElseThrow(
                ()->new RuntimeException("Workflow not found"));
        if(workFlow.getRetries()<3 && (workFlow.getRetriedAt()==null ||
                                workFlow.getRetriedAt().isBefore(LocalDateTime.now()))) {
            workFlow.setRetries(workFlow.getRetries()+1);
            workFlow.setRetriedAt(LocalDateTime.now().plusSeconds(30));
            StepStatus result = createStep(workFlow,
                    workFlow.getCurrentStep(),
                    WorkFlowStepName.valueOf(workFlow.getCurrentStep().toUpperCase()).number(), workFlow.getPayload());
            if (result == StepStatus.SUCCESS) {
                    workFlow.setStatus(WorkFlowStatus.COMPLETED);
                     workFlowRepository.save(workFlow);
                log.info("Retry is successful for {}",workflowId);
                    return "Retry is successful";
            }else {
                workFlow.setStatus(WorkFlowStatus.RETRY_QUERY);
                workFlowRepository.save(workFlow);
                log.info("Retry is unsuccessful for {}",workflowId);
                return "Retry is unsuccessful";
            }
        }else if(workFlow.getRetries()>=3){
            workFlow.setStatus(WorkFlowStatus.COMPENSATING_INVENTORY);
            workFlow=workFlowRepository.save(workFlow);
            compensateInventory(workFlow.getWorkFlowId());
            log.info("Retry Limit is over so we are compensating the workflow for {}",workflowId);
            return "Retry Limit is over so we are compensating the workflow";
        }
        log.info("Either Retry limit Over or Retry time is to early check again try after some time for {}",workflowId);
        return "Either Retry limit Over or Retry time is to early check again try after some time ";
    }

    public String compensatePayment(Long workflowId) {
        log.info("compensatePayment method called for {}",workflowId);
        WorkFlow workFlow=workFlowRepository.findById(workflowId).orElseThrow(
                ()->new GlobalExceptionHandler.ResourceNotFoundException("Workflow not found"));
        if(workFlow.getStatus()==WorkFlowStatus.COMPENSATING_PAYMENT){
        ServerResponse serverResponse=paymentService.compensatePayment(workFlow.getWorkFlowId()).getBody();
            if(serverResponse.getStatus().startsWith("COMPENSATED")) {
                workFlow.setStatus(WorkFlowStatus.COMPENSATED_PAYMENT);
                workFlowRepository.save(workFlow);
                log.info("COMPENSATED PAYMENT for {}",workflowId);
                return "COMPENSATED PAYMENT";
            }else {
                workFlow.setStatus(WorkFlowStatus.FAILED);
                workFlowRepository.save(workFlow);
                log.info("Compensation failed for payment for {}",workflowId);
                return "Compensation failed for payment";
            }
        }else{
            log.info("Money didn't even get deducted for {}",workflowId);
            return "Money didn't even get deducted";
        }
    }

    public String compensateInventory(Long workflowId){
        log.info("compensateInventory method called for {}",workflowId);
    WorkFlow workFlow=workFlowRepository.findById(workflowId).orElseThrow(
                ()->new GlobalExceptionHandler.ResourceNotFoundException("Workflow not found"));
        if(workFlow.getStatus()==WorkFlowStatus.COMPENSATING_INVENTORY) {
            ServerResponse serverResponse = inventoryService.releaseStock(workFlow.getWorkFlowId()).getBody();
            if (serverResponse.getStatus().startsWith("COMPENSATED")) {
                workFlow.setStatus(WorkFlowStatus.COMPENSATING_PAYMENT);
                workFlow = workFlowRepository.save(workFlow);
                String result = compensatePayment(workflowId);
                if (result.equals("COMPENSATED PAYMENT")) {
                    workFlow.setStatus(WorkFlowStatus.COMPENSATED_PAYMENT);
                    workFlowRepository.save(workFlow);
                    log.info("Compensated inventory and payment for {}",workflowId);
                    return "Compensated inventory and payment";
                } else {
                    log.info("Compensated inventory but not payment for {}",workflowId);
                    return "Compensated inventory but not payment" +compensatePayment(workFlow.getWorkFlowId());
                }
            }else {
                workFlow.setLastError(serverResponse.getErrorMessage());
                workFlowRepository.save(workFlow);
                log.info("Compensate failed for inventory for {}",workflowId);
                return "Compensate failed for inventory";
            }
        }else{
            log.info("Inventory didn't even reserved the product for {}",workflowId);
            return "Inventory didn't even reserved the product";
        }
}

    private WorkFlowResponse workFlowToWorkFlowResponse(WorkFlow workFlow) {
        log.info("workFlowToWorkFlowResponse method called for {}",workFlow.toString());
        WorkFlowResponse response=new WorkFlowResponse();
        response.setWorkflowId(workFlow.getWorkFlowId());
        response.setStatus(workFlow.getStatus().toString());
        response.setCurrentStep(String.valueOf(workFlow.getCurrentStep()));
        response.setCreatedAt(workFlow.getCreatedAt());
        response.setUpdatedAt(workFlow.getUpdatedAt());
        response.setLastError(workFlow.getLastError());
        return response;
    }
    @Transactional
    public StepStatus createStep(WorkFlow workflow,String stepName,int stepOrder,String payload){
        log.info("createStep method called for {} and stepName {}",workflow.getWorkFlowId(),stepName);
        WorkFlowStep workFlowStep=new WorkFlowStep();
        String id= UUID.randomUUID().toString();
        workFlowStep.setStepId(id);
        workFlowStep.setWorkFlowId(workflow.getWorkFlowId());
        workFlowStep.setStepName(stepName);
        workFlowStep.setStepOrder(stepOrder);
        workFlowStep.setRequestPayload(payload);
        workFlowStep.setStatus(StepStatus.PENDING);
        workFlowStep=workFlowStepRepository.save(workFlowStep);

        ServiceRequest serviceRequest =new ServiceRequest();
        serviceRequest.setWorkflowId(workFlowStep.getWorkFlowId());
        serviceRequest.setUserId(workflow.getUserId());
        serviceRequest.setProductId(workflow.getProductId());
        serviceRequest.setWorkFlowStepId(workFlowStep.getStepId());
        serviceRequest.setQuantity(workflow.getQuantity());
        serviceRequest.setCalledTimer(LocalDateTime.now());
        try{
            //Feign client calls
             ServerResponse sr = switch (WorkFlowStepName.valueOf(stepName)) {
                case PAYMENT -> paymentService.processPayment(serviceRequest).getBody();
                case INVENTORY -> inventoryService.reserveStock(serviceRequest).getBody();
                case NOTIFICATION ->{
                        ServerResponse ServerResponse= new ServerResponse();
                    ServerResponse.setStatus(messageService.sendNotificationForOrderSuccess(serviceRequest));
                    yield ServerResponse;
                }
            };
            if(sr.getStatus().startsWith("SUCCESS")){
                log.info("createStep success for {} and stepName {}",workflow.getWorkFlowId(),stepName);
                workFlowStep.setStatus(StepStatus.SUCCESS);
                workFlowStep.setResponsePayload(objectMapper.writeValueAsString(sr));
                workFlowStepRepository.save(workFlowStep);
                return StepStatus.SUCCESS;
            } else if (sr.getStatus().startsWith("FAILED")) {
                log.info("createStep failed for {} and stepName {}",workflow.getWorkFlowId(),stepName);
                workFlowStep.setStatus(StepStatus.FAILED);
                workFlowStep.setResponsePayload(objectMapper.writeValueAsString(sr));
                workFlowStep.setErrorMessage(sr.getStatus());
                workFlowStepRepository.save(workFlowStep);
                return StepStatus.FAILED;
            }else {
                log.info("createStep failed due to technical failure for {} and stepName {}",workflow.getWorkFlowId(),stepName);
                workFlowStep.setStatus(StepStatus.TECHNICAL_FAILURE);
                workFlowStep.setErrorMessage(sr.getStatus());
                workFlowStep.setResponsePayload(objectMapper.writeValueAsString(sr));
                workFlowStepRepository.save(workFlowStep);
                return StepStatus.TECHNICAL_FAILURE;
            }
        }catch (Exception e){
            log.error("Step execution failed for {} and stepName {} and reason: {}",workflow.getWorkFlowId(),stepName, e.getMessage());
            workFlowStep.setStatus(StepStatus.FAILED);
            workFlowStep.setErrorMessage(e.getMessage());
            workFlowStepRepository.save(workFlowStep);
            return StepStatus.FAILED;
        }
    }

    @Override
    public String compensateInventoryManually(long workFlowId) {
        log.info("compensateInventoryManually method called for: {}",workFlowId);
        WorkFlow workFlow=workFlowRepository.findById(workFlowId).orElseThrow(
                ()->new GlobalExceptionHandler.ResourceNotFoundException("Workflow not found"));
        if((workFlow.getStatus()==WorkFlowStatus.COMPLETED || (workFlow.getRetries()>=3 && workFlow.getStatus()==WorkFlowStatus.COMPENSATING_INVENTORY))
                && workFlow.getCurrentStep().equals(WorkFlowStepName.NOTIFICATION.toString())){
            log.info("compensateInventoryManually method called and went to if block for: {}",workFlowId);
            workFlow.setStatus(WorkFlowStatus.COMPENSATING_INVENTORY);
            workFlowRepository.save(workFlow);
            return  compensateInventory(workFlowId);
        }
        log.info("compensateInventoryManually method called and didn't went to if block for: {}",workFlowId);
        return "Either Inventory and Payment are Compensated or in Progress if Payment and Inventory Successfully or else calling this endpoint is waste ";
    }
    @Override
    public String compensatePaymentManually(long workFlowId) {
        log.info("compensatePaymentManually method called for: {}",workFlowId);
        WorkFlow workFlow=workFlowRepository.findById(workFlowId).orElseThrow(
                ()->new GlobalExceptionHandler.ResourceNotFoundException("Workflow not found"));
        if((workFlow.getStatus()==WorkFlowStatus.FAILED || workFlow.getStatus()==WorkFlowStatus.COMPENSATING_PAYMENT)
                && workFlow.getCurrentStep().equals(WorkFlowStepName.INVENTORY.toString())){
            log.info("workFlowRepository method called and went to if block for: {}",workFlowId);
            workFlow.setStatus(WorkFlowStatus.COMPENSATING_PAYMENT);
            workFlowRepository.save(workFlow);
            return  compensatePayment(workFlowId);
        }
        log.info("compensatePaymentManually method called and didn't went to if block for: {}",workFlowId);
        return "Payment are Compensated or in Progress if Payment Successfully or else calling this endpoint is waste ";
    }


}
