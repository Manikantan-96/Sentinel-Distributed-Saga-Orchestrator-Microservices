package com.sentinelorchestratorservice.Controller;

import com.sentinelorchestratorservice.DTO.Request.WorkFlowRequest;
import com.sentinelorchestratorservice.DTO.Response.WorkFlowDetailsResponse;
import com.sentinelorchestratorservice.Service.WorkFlowService;
import com.sentinelorchestratorservice.Service.WorkFlowServiceMethods;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
public class WorkFlowController {
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkFlowServiceMethods workFlowServiceMethods;

    @PostMapping("/start")
    public ResponseEntity<WorkFlowDetailsResponse> workflow(@Valid @RequestBody WorkFlowRequest workFlowRequest){
        log.info("Request received for start workflow{}",workFlowRequest.toString());
       return ResponseEntity.ok(workFlowService.createWorkflow(workFlowRequest));
    }
    @GetMapping("/id/{workFlowId}")
    public ResponseEntity<WorkFlowDetailsResponse> getWorkflowById(@PathVariable long workFlowId){
        log.info("Request received for get Workflow By Id{}",workFlowId);
        return ResponseEntity.ok(workFlowServiceMethods.getWorkFlowById(workFlowId));
    }
    @GetMapping("/id/{workFlowId}/retry")
    public ResponseEntity<String> retryWorkflow(@PathVariable long workFlowId){
        log.info("Request received for retry Workflow By Id{}",workFlowId);
        return ResponseEntity.ok(workFlowServiceMethods.RetryWorkflow(workFlowId));
    }
    @GetMapping("/resume")
    public ResponseEntity<String> resumePendingServices() {
        log.info("Request received for resume Pending Services");
        workFlowService.resumePendingServices();
        return ResponseEntity.ok("Resume Job Triggered Successfully");
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getWorkflows(@PathVariable(required = false) String status){
        log.info("Request received for get Workflows by Status{}",status);
        return ResponseEntity.ok(workFlowServiceMethods.ListOfWorkFlows(status));
    }
    @PostMapping("/compentation/payment/{workFlowId}")
    public ResponseEntity<String> compensateWorkflowForPayment(@PathVariable long workFlowId){
        log.info("Request received for compensate Workflow For Payment Id{}",workFlowId);
        return ResponseEntity.ok(workFlowServiceMethods.compensatePaymentManually(workFlowId));
    }
    @PostMapping("/compentation/inventory/{workFlowId}")
    public ResponseEntity<String> compensateWorkflow(@PathVariable long workFlowId){
        log.info("Request received for compensate Workflow For Inventory Id{}",workFlowId);
        return ResponseEntity.ok(workFlowServiceMethods.compensateInventoryManually(workFlowId));
    }

}
