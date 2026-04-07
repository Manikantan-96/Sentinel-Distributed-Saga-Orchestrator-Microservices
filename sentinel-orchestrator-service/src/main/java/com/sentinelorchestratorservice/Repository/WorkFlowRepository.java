package com.sentinelorchestratorservice.Repository;

import com.sentinelorchestratorservice.Entity.WorkFlow;
import com.sentinelorchestratorservice.Enum.WorkFlowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WorkFlowRepository extends JpaRepository<WorkFlow, Long> {
    List<WorkFlow> findByStatus(WorkFlowStatus status);
    List<WorkFlow> findByStatusIn(List<WorkFlowStatus> statuses);

}