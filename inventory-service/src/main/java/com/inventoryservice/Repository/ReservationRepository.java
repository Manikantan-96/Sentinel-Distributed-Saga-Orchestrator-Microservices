package com.inventoryservice.Repository;

import com.inventoryservice.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation,String> {
    Reservation findByWorkflowId(Long workflowId);
}
