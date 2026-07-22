package com.infosys.CivicsApp.repository;

import com.infosys.CivicsApp.entity.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GrievanceRepository extends JpaRepository<Grievance, UUID> {
    long countByEscalated(boolean escalated);
    List<Grievance> findBySlaDeadlineBeforeAndStatusNotInAndEscalatedFalse(LocalDateTime now, Collection<String> statuses);
    List<Grievance> findAllByOrderByCreatedDateDesc();
    List<Grievance> findByCitizenIdOrderByCreatedDateDesc(UUID citizenId);
    List<Grievance> findByAssignedOfficerIdOrderByCreatedDateDesc(UUID officerId);
    List<Grievance> findByDepartmentIdOrderByCreatedDateDesc(UUID departmentId);
    boolean existsByCitizenIdAndTitleAndStatusNotIn(UUID citizenId, String title, Collection<String> statuses);
}
