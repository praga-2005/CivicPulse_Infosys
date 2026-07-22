package com.infosys.CivicsApp.client;

import com.infosys.CivicsApp.dto.CitizenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "citizen-service", url = "http://localhost:8081")
public interface CitizenClient {

    @GetMapping("/api/citizens/{id}")
    CitizenDto getCitizenById(@PathVariable("id") UUID id);

    @GetMapping("/api/citizens")
    List<CitizenDto> getAllCitizens();

    @GetMapping("/api/citizens/count")
    long getCitizenCount();
}
