package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.dto.CitizenRegisteredEvent;
import com.infosys.CivicsApp.entity.Citizen;
import com.infosys.CivicsApp.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CitizenService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired(required = false)
    private KafkaTemplate<String, CitizenRegisteredEvent> kafkaTemplate;

    public Citizen registerCitizen(Citizen citizen) {
        if (citizen.getName() == null || citizen.getName().trim().isEmpty() ||
            citizen.getEmail() == null || citizen.getEmail().trim().isEmpty() ||
            citizen.getPassword() == null || citizen.getPassword().trim().isEmpty() ||
            citizen.getPhone() == null || citizen.getPhone().trim().isEmpty() ||
            citizen.getAddress() == null || citizen.getAddress().trim().isEmpty() ||
            citizen.getWard() == null || citizen.getWard().trim().isEmpty() ||
            citizen.getCity() == null || citizen.getCity().trim().isEmpty() ||
            citizen.getPin() == null || citizen.getPin().trim().isEmpty() ||
            citizen.getAadhar() == null || citizen.getAadhar().trim().isEmpty()) {

            throw new RuntimeException("All required fields must be filled!");
        }

        if (citizen.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long!");
        }

        if (citizenRepository.existsByEmail(citizen.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        if (citizenRepository.existsByPhone(citizen.getPhone())) {
            throw new RuntimeException("Phone number is already registered!");
        }

        Citizen savedCitizen = citizenRepository.save(citizen);
        sendCitizenRegisteredEvent(savedCitizen);
        return savedCitizen;
    }

    private void sendCitizenRegisteredEvent(Citizen citizen) {
        if (kafkaTemplate == null) {
            System.out.println("KafkaTemplate is not configured or disabled. Skipping Kafka event publish.");
            return;
        }
        try {
            CitizenRegisteredEvent event = new CitizenRegisteredEvent(
                citizen.getId(),
                citizen.getName(),
                citizen.getEmail(),
                citizen.getWard()
            );
            kafkaTemplate.send("citizen-events", citizen.getId().toString(), event);
            System.out.println("Published CitizenRegisteredEvent to Kafka: " + event);
        } catch (Exception e) {
            System.err.println("Failed to publish CitizenRegisteredEvent to Kafka: " + e.getMessage());
        }
    }

    public Citizen loginCitizen(String email, String password) {
        Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email);
        if (citizenOpt.isEmpty()) {
            throw new RuntimeException("Citizen not found!");
        }

        Citizen citizen = citizenOpt.get();
        if (!citizen.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password!");
        }

        return citizen;
    }

    public Citizen getCitizenById(UUID id) {
        return citizenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Citizen not found!"));
    }

    public List<Citizen> getAllCitizens() {
        return citizenRepository.findAll();
    }

    public long getCitizenCount() {
        return citizenRepository.count();
    }

    public Citizen updateCitizenProfile(UUID id, Citizen updatedDetails) {
        Citizen citizen = getCitizenById(id);

        if (updatedDetails.getName() != null && !updatedDetails.getName().trim().isEmpty()) {
            citizen.setName(updatedDetails.getName());
        }

        if (updatedDetails.getPhone() != null && !updatedDetails.getPhone().trim().isEmpty()) {
            if (!citizen.getPhone().equals(updatedDetails.getPhone())
                    && citizenRepository.existsByPhone(updatedDetails.getPhone())) {
                throw new RuntimeException("Phone number is already registered by another user!");
            }
            citizen.setPhone(updatedDetails.getPhone());
        }

        if (updatedDetails.getAddress() != null && !updatedDetails.getAddress().trim().isEmpty()) {
            citizen.setAddress(updatedDetails.getAddress());
        }

        if (updatedDetails.getWard() != null && !updatedDetails.getWard().trim().isEmpty()) {
            citizen.setWard(updatedDetails.getWard());
        }

        if (updatedDetails.getCity() != null && !updatedDetails.getCity().trim().isEmpty()) {
            citizen.setCity(updatedDetails.getCity());
        }

        if (updatedDetails.getPin() != null && !updatedDetails.getPin().trim().isEmpty()) {
            citizen.setPin(updatedDetails.getPin());
        }

        if (updatedDetails.getAadhar() != null && !updatedDetails.getAadhar().trim().isEmpty()) {
            citizen.setAadhar(updatedDetails.getAadhar());
        }

        return citizenRepository.save(citizen);
    }
}
