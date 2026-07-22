package com.infosys.CivicsApp.repository;

import com.infosys.CivicsApp.entity.ServiceApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ServiceApplication, Long> {
    List<ServiceApplication> findByCitizenId(String citizenId);
    List<ServiceApplication> findByStatus(String status);
    List<ServiceApplication> findByServiceType(String serviceType);
    boolean existsByCitizenIdAndServiceTypeAndStatusNot(String citizenId, String serviceType, String status);
}
