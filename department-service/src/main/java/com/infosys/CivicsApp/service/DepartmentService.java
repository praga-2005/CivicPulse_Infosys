package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.entity.Department;
import com.infosys.CivicsApp.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepartmentService implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found!"));
    }

    public long getDepartmentCount() {
        return departmentRepository.count();
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed departments if empty
        if (departmentRepository.count() == 0) {
            createDepartment("Water Supply", "Handles water leakage, sewer, water distribution issues.");
            createDepartment("Roads & Traffic", "Handles potholes, street signals, road repairs, illegal parking.");
            createDepartment("Sanitation & Waste", "Handles garbage collection, street sweeping, cleanliness.");
            createDepartment("Electricity", "Handles street light failures, power line outages, electrical safety.");
            createDepartment("Parks & Recreation", "Handles fallen trees, public park maintenance, lawn mowing.");
        }
    }

    private void createDepartment(String name, String description) {
        Department dept = new Department();
        // Generate deterministic UUID
        UUID deterministicId = UUID.nameUUIDFromBytes(name.getBytes());
        dept.setId(deterministicId);
        dept.setName(name);
        dept.setDescription(description);
        departmentRepository.save(dept);
    }
}
