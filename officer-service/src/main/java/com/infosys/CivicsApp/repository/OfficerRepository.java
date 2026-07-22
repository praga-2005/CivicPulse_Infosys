package com.infosys.CivicsApp.repository;

import com.infosys.CivicsApp.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfficerRepository extends JpaRepository<Officer, UUID> {
    Optional<Officer> findByEmail(String email);
    List<Officer> findByDepartmentId(UUID departmentId);
}
