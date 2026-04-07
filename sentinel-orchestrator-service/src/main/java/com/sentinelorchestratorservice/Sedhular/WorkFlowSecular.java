package com.sentinelorchestratorservice.Sedhular;

import com.sentinelorchestratorservice.Entity.WorkFlow;
import com.sentinelorchestratorservice.Enum.WorkFlowStatus;
import com.sentinelorchestratorservice.Repository.WorkFlowRepository;
import com.sentinelorchestratorservice.Service.WorkFlowService;
import com.sentinelorchestratorservice.Service.WorkFlowServiceMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkFlowSecular {
    @Autowired
    private WorkFlowServiceMethods workFlowServiceMethods;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkFlowRepository workFlowRepository;
    @Scheduled(fixedDelay = 60000)
    public void resumePendingServices() {
        workFlowService.resumePendingServices();
    }
    @Scheduled(fixedDelay = 60000)
    public void RetryQuery() {
       List<WorkFlow> workFlows= workFlowRepository.findByStatus(WorkFlowStatus.RETRY_QUERY);
       for(WorkFlow workFlow:workFlows)
        workFlowServiceMethods.RetryWorkflow(workFlow.getWorkFlowId());
    }


}
