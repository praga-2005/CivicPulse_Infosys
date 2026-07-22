package com.infosys.CivicsApp.controller;

import com.infosys.CivicsApp.dto.RejectServiceRequest;
import com.infosys.CivicsApp.dto.VerifyServiceRequest;
import com.infosys.CivicsApp.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services/officer")
public class OfficerController {

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications() {
        // Here we could distinguish between Verification Pending and Approval Pending
        // For simplicity, let's return verification pending
        return ResponseEntity.ok(applicationService.getPendingVerification());
    }

    @GetMapping("/approvals")
    public ResponseEntity<?> getPendingApprovals() {
        return ResponseEntity.ok(applicationService.getPendingApproval());
    }

    @PutMapping("/verify/{applicationId}")
    public ResponseEntity<?> verifyDocuments(@PathVariable Long applicationId, @RequestBody VerifyServiceRequest request) {
        try {
            return ResponseEntity.ok(applicationService.verifyDocuments(applicationId, request.isVerified()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/approve/{applicationId}")
    public ResponseEntity<?> approveApplication(@PathVariable Long applicationId) {
        try {
            return ResponseEntity.ok(applicationService.approveApplication(applicationId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/reject/{applicationId}")
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId, @RequestBody RejectServiceRequest request) {
        try {
            return ResponseEntity.ok(applicationService.rejectApplication(applicationId, request.getReason()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
