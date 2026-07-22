package com.infosys.CivicsApp.client;

import com.infosys.CivicsApp.dto.OfficerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "officer-service", url = "http://localhost:8083")
public interface OfficerClient {

    @GetMapping("/api/officers/{id}")
    OfficerDto getOfficerById(@PathVariable("id") UUID id);

    @GetMapping("/api/officers/department/{deptId}")
    List<OfficerDto> getOfficersByDepartment(@PathVariable("deptId") UUID deptId);

    @GetMapping("/api/officers")
    List<OfficerDto> getAllOfficers();

    @GetMapping("/api/officers/count")
    long getOfficerCount();
}
