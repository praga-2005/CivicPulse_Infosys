package com.infosys.CivicsApp.controller;

import com.infosys.CivicsApp.entity.Citizen;
import com.infosys.CivicsApp.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Citizen citizen) {
        try {
            Citizen registered = citizenService.registerCitizen(citizen);
            return ResponseEntity.ok(registered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            Citizen citizen = citizenService.loginCitizen(email, password);
            return ResponseEntity.ok(Map.of(
                "citizen", citizen
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Citizen>> getAllCitizens() {
        return ResponseEntity.ok(citizenService.getAllCitizens());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getCitizenCount() {
        return ResponseEntity.ok(citizenService.getCitizenCount());
    }

    @GetMapping({"/profile/{id}", "/{id}"})
    public ResponseEntity<?> getProfile(@PathVariable UUID id) {
        try {
            Citizen citizen = citizenService.getCitizenById(id);
            return ResponseEntity.ok(citizen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping({"/profile/{id}", "/{id}"})
    public ResponseEntity<?> updateProfile(@PathVariable UUID id, @RequestBody Citizen citizen) {
        try {
            Citizen updated = citizenService.updateCitizenProfile(id, citizen);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
