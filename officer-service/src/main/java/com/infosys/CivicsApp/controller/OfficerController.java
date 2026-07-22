package com.infosys.CivicsApp.controller;

import com.infosys.CivicsApp.entity.Officer;
import com.infosys.CivicsApp.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    @Autowired
    private OfficerService officerService;


    // Login Officer/Admin
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            Officer officer = officerService.loginOfficer(email, password);

            return ResponseEntity.ok(
                    Map.of(
                            "officer", officer
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Create Officer/Admin
    @PostMapping
    public ResponseEntity<?> createOfficer(@RequestBody Officer officer) {
        try {
            Officer savedOfficer = officerService.registerOfficer(officer);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedOfficer);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Get All Officers
    @GetMapping
    public ResponseEntity<List<Officer>> getAllOfficers() {
        return ResponseEntity.ok(officerService.getAllOfficers());
    }

    // Get Officer By ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOfficerById(@PathVariable UUID id) {
        try {
            Officer officer = officerService.getOfficerById(id);
            return ResponseEntity.ok(officer);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Officer not found"));
        }
    }

    // Get Officers By Department
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<Officer>> getOfficersByDepartment(
            @PathVariable UUID deptId) {

        return ResponseEntity.ok(
                officerService.getOfficersByDepartment(deptId)
        );
    }

    // Get Officer Count
    @GetMapping("/count")
    public ResponseEntity<Long> getOfficerCount() {
        return ResponseEntity.ok(
                officerService.getOfficerCount()
        );
    }
}