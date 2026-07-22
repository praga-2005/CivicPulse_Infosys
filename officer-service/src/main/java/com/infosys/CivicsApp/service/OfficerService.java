package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.client.DepartmentClient;
import com.infosys.CivicsApp.dto.DepartmentDto;
import com.infosys.CivicsApp.entity.Officer;
import com.infosys.CivicsApp.repository.OfficerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfficerService implements CommandLineRunner {

    @Autowired
    private OfficerRepository officerRepository;

    @Autowired
    private DepartmentClient departmentClient;

    public Officer loginOfficer(String email, String password) {
        Optional<Officer> officerOpt = officerRepository.findByEmail(email);
        if (officerOpt.isEmpty() || !officerOpt.get().getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password!");
        }
        return populateDepartment(officerOpt.get());
    }

    public Officer getOfficerById(UUID id) {
        Officer officer = officerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Officer not found!"));
        return populateDepartment(officer);
    }

    public List<Officer> getAllOfficers() {
        return officerRepository.findAll().stream()
                .map(this::populateDepartment)
                .collect(Collectors.toList());
    }

    public List<Officer> getOfficersByDepartment(UUID departmentId) {
        return officerRepository.findByDepartmentId(departmentId).stream()
                .map(this::populateDepartment)
                .collect(Collectors.toList());
    }

    public long getOfficerCount() {
        return officerRepository.count();
    }

    private Officer populateDepartment(Officer officer) {
        if (officer != null && officer.getDepartmentId() != null) {
            try {
                DepartmentDto dept = departmentClient.getDepartmentById(officer.getDepartmentId());
                officer.setDepartment(dept);
            } catch (Exception e) {
                // Log and swallow if department service is offline
                System.err.println("Could not load department for officer: " + e.getMessage());
            }
        }
        return officer;
    }

    public Officer registerOfficer(Officer officer) {
        return officerRepository.save(officer);
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Officers if empty
        if (officerRepository.count() == 0) {
            // Generate deterministic department UUIDs corresponding to department-service
            UUID waterId = UUID.nameUUIDFromBytes("Water Supply".getBytes());
            UUID roadId = UUID.nameUUIDFromBytes("Roads & Traffic".getBytes());
            UUID sanitationId = UUID.nameUUIDFromBytes("Sanitation & Waste".getBytes());
            UUID electricityId = UUID.nameUUIDFromBytes("Electricity".getBytes());

            createOfficer("Ramesh Kumar", "ramesh@gov.in", "officer123", waterId, "OFFICER");
            createOfficer("Suresh Singh", "suresh@gov.in", "officer123", roadId, "OFFICER");
            createOfficer("Anita Roy", "anita@gov.in", "officer123", sanitationId, "OFFICER");
            createOfficer("Vikram Malhotra", "vikram@gov.in", "officer123", electricityId, "OFFICER");
            
            // System Admin
            createOfficer("Admin Executive", "admin@gov.in", "admin123", null, "ADMIN");
        }

        // Add custom requested members if they don't exist
        if (officerRepository.findByEmail("johndoe@gov.in").isEmpty()) {
            UUID waterId = UUID.nameUUIDFromBytes("Water Supply".getBytes());
            createOfficer("John Doe", "johndoe@gov.in", "officer123", waterId, "OFFICER");
        }
        if (officerRepository.findByEmail("janesmith@gov.in").isEmpty()) {
            createOfficer("Jane Smith", "janesmith@gov.in", "admin123", null, "ADMIN");
        }
        if (officerRepository.findByEmail("admin@gmail.com").isEmpty()) {
            createOfficer("Standard Admin", "admin@gmail.com", "admin", null, "ADMIN");
        }
    }

    private void createOfficer(String name, String email, String password, UUID departmentId, String role) {
        Officer officer = new Officer();
        officer.setName(name);
        officer.setEmail(email);
        officer.setPassword(password);
        officer.setDepartmentId(departmentId);
        officer.setRole(role);
        try {
            officerRepository.save(officer);
        } catch (Exception e) {
            System.err.println("Could not seed officer " + email + ": " + e.getMessage());
        }
    }
}
