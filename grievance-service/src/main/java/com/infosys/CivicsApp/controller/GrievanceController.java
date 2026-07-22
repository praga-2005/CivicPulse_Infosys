package com.infosys.CivicsApp.controller;

import com.infosys.CivicsApp.entity.Grievance;
import com.infosys.CivicsApp.entity.GrievanceHistory;
import com.infosys.CivicsApp.service.GrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/grievances")
public class GrievanceController {

    @Autowired
    private GrievanceService grievanceService;

    @PostMapping
    public ResponseEntity<?> createGrievance(@RequestBody Map<String, String> request) {
        try {
            UUID citizenId = UUID.fromString(request.get("citizenId"));
            UUID departmentId = UUID.fromString(request.get("departmentId"));
            String title = request.get("title");
            String description = request.get("description");
            String location = request.get("location");
            String priority = request.get("priority");

            Grievance grievance = grievanceService.createGrievance(
                    citizenId, departmentId, title, description, location, priority
            );
            return ResponseEntity.ok(grievance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Grievance>> getGrievances(
            @RequestParam(required = false) UUID citizenId,
            @RequestParam(required = false) UUID officerId,
            @RequestParam(required = false) UUID departmentId
    ) {
        if (citizenId != null) {
            return ResponseEntity.ok(grievanceService.getGrievancesByCitizen(citizenId));
        } else if (officerId != null) {
            return ResponseEntity.ok(grievanceService.getGrievancesByOfficer(officerId));
        } else if (departmentId != null) {
            return ResponseEntity.ok(grievanceService.getGrievancesByDepartment(departmentId));
        } else {
            return ResponseEntity.ok(grievanceService.getAllGrievances());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGrievanceById(@PathVariable UUID id) {
        try {
            Grievance grievance = grievanceService.getGrievanceById(id);
            return ResponseEntity.ok(grievance);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<GrievanceHistory>> getGrievanceHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(grievanceService.getGrievanceHistory(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request
    ) {
        try {
            String status = request.get("status");
            String updatedBy = request.get("updatedBy");
            String remarks = request.get("remarks");

            Grievance updated = grievanceService.updateGrievanceStatus(id, status, updatedBy, remarks);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignOfficer(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request
    ) {
        try {
            UUID officerId = UUID.fromString(request.get("officerId"));
            String updatedBy = request.get("updatedBy");

            Grievance updated = grievanceService.assignOfficer(id, officerId, updatedBy);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/escalate-trigger")
    public ResponseEntity<?> triggerEscalations() {
        try {
            grievanceService.escalateOverdueGrievances();
            return ResponseEntity.ok(Map.of("message", "Escalation rules run successfully. Overdue grievances processed."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGrievance(@PathVariable UUID id) {
        try {
            grievanceService.deleteGrievance(id);
            return ResponseEntity.ok(Map.of("message", "Grievance and its history timeline deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
