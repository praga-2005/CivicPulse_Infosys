package com.infosys.CivicsApp.client;

import com.infosys.CivicsApp.dto.DepartmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "department-service", url = "http://localhost:8082")
public interface DepartmentClient {

    @GetMapping("/api/departments/{id}")
    DepartmentDto getDepartmentById(@PathVariable("id") UUID id);

    @GetMapping("/api/departments")
    List<DepartmentDto> getAllDepartments();

    @GetMapping("/api/departments/count")
    long getDepartmentCount();
}
