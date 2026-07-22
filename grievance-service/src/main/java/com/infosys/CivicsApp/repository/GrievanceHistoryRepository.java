package com.infosys.CivicsApp.repository;

import com.infosys.CivicsApp.entity.GrievanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GrievanceHistoryRepository extends JpaRepository<GrievanceHistory, UUID> {
    List<GrievanceHistory> findByGrievanceIdOrderByTimestampAsc(UUID grievanceId);
}
