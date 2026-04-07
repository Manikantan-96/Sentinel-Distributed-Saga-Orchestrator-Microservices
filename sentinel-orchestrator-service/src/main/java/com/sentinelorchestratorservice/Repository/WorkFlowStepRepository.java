package com.sentinelorchestratorservice.Repository;

import com.sentinelorchestratorservice.Entity.WorkFlowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WorkFlowStepRepository extends JpaRepository<WorkFlowStep, String> {
    List<WorkFlowStep> findByWorkFlowIdOrderByStepOrderAsc(long workFlowId);
}
