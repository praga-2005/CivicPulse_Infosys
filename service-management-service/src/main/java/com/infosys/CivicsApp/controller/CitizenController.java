package com.infosys.CivicsApp.controller;

import com.infosys.CivicsApp.dto.ApplyServiceRequest;
import com.infosys.CivicsApp.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class CitizenController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyCertificate(@RequestBody ApplyServiceRequest request) {
        try {
            return ResponseEntity.ok(applicationService.applyForService(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<?> getMyApplications(@PathVariable String citizenId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCitizen(citizenId));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<?> getApplicationDetails(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(applicationService.getApplicationById(applicationId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{applicationId}")
    public ResponseEntity<?> downloadCertificate(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(applicationService.recordDownload(applicationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
