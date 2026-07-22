package com.infosys.CivicsApp.repository;

import com.infosys.CivicsApp.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CitizenRepository extends JpaRepository<Citizen, UUID> {
    Optional<Citizen> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
