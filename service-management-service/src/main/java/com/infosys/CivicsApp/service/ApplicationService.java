package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.dto.ApplyServiceRequest;
import com.infosys.CivicsApp.dto.ServiceApplicationEvent;
import com.infosys.CivicsApp.entity.ServiceApplication;
import com.infosys.CivicsApp.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository repository;

    @Autowired
    private KafkaTemplate<String, ServiceApplicationEvent> kafkaTemplate;

    public ServiceApplication applyForService(ApplyServiceRequest request) {
        // Milestone 2: Prevent duplicate applications unless rejected
        boolean exists = repository.existsByCitizenIdAndServiceTypeAndStatusNot(
                request.getCitizenId(), request.getServiceType(), "REJECTED");
        if (exists) {
            throw new RuntimeException("You have already applied for this certificate.");
        }

        ServiceApplication app = new ServiceApplication();
        app.setApplicationNumber("APP-" + System.currentTimeMillis() + "-" + (int)(Math.random()*1000));
        app.setCitizenId(request.getCitizenId());
        app.setServiceType(request.getServiceType());
        app.setApplicantName(request.getApplicantName());
        app.setAadhaarNumber(request.getAadhaarNumber());
        app.setStatus("SUBMITTED");
        app.setVerificationStatus("PENDING");
        app.setApprovalStatus("PENDING");

        ServiceApplication saved = repository.save(app);

        publishEvent(new ServiceApplicationEvent("ApplicationSubmittedEvent", saved.getId(), saved.getApplicationNumber(), saved.getCitizenId()));
        
        return saved;
    }

    public ServiceApplication verifyDocuments(Long id, boolean verified) {
        ServiceApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        if (verified) {
            app.setVerificationStatus("VERIFIED");
            app.setStatus("UNDER_VERIFICATION"); // Moving down workflow
            // Update to VERIFIED if requested by document
            app.setStatus("VERIFIED"); 
            publishEvent(new ServiceApplicationEvent("DocumentVerifiedEvent", app.getId(), app.getApplicationNumber(), app.getCitizenId()));
        } else {
            app.setVerificationStatus("FAILED");
            app.setStatus("REJECTED");
        }
        return repository.save(app);
    }

    public ServiceApplication approveApplication(Long id) {
        ServiceApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        if (!"VERIFIED".equals(app.getStatus())) {
            throw new RuntimeException("Application must be verified before approval");
        }
        app.setApprovalStatus("APPROVED");
        app.setStatus("APPROVED");
        app.setApprovedDate(LocalDateTime.now());
        repository.save(app);

        publishEvent(new ServiceApplicationEvent("CertificateApprovedEvent", app.getId(), app.getApplicationNumber(), app.getCitizenId()));
        
        // Auto-generate certificate
        return generateCertificate(app);
    }

    public ServiceApplication rejectApplication(Long id, String reason) {
        ServiceApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        app.setApprovalStatus("REJECTED");
        app.setStatus("REJECTED");
        // Could store reason in a new field or history log
        return repository.save(app);
    }

    private ServiceApplication generateCertificate(ServiceApplication app) {
        String typeCode = app.getServiceType().replaceAll("[^A-Z]", "");
        if (typeCode.isEmpty()) typeCode = "CERT";
        app.setCertificateNumber(typeCode + "-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", app.getId()));
        app.setStatus("CERTIFICATE_GENERATED");
        ServiceApplication saved = repository.save(app);

        publishEvent(new ServiceApplicationEvent("CertificateGeneratedEvent", saved.getId(), saved.getApplicationNumber(), saved.getCitizenId()));
        return saved;
    }

    public ServiceApplication recordDownload(Long id) {
        ServiceApplication app = repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
        if (!"CERTIFICATE_GENERATED".equals(app.getStatus())) {
            throw new RuntimeException("Certificate not generated yet");
        }
        app.setDownloadCount(app.getDownloadCount() + 1);
        return repository.save(app);
    }

    public List<ServiceApplication> getApplicationsByCitizen(String citizenId) {
        return repository.findByCitizenId(citizenId);
    }

    public List<ServiceApplication> getAllApplications() {
        return repository.findAll();
    }

    public List<ServiceApplication> getPendingVerification() {
        return repository.findByStatus("SUBMITTED");
    }

    public List<ServiceApplication> getPendingApproval() {
        return repository.findByStatus("VERIFIED");
    }

    public ServiceApplication getApplicationById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    private void publishEvent(ServiceApplicationEvent event) {
        try {
            kafkaTemplate.send("service-events", String.valueOf(event.getApplicationId()), event);
        } catch (Exception e) {
            System.err.println("Failed to publish Kafka event: " + e.getMessage());
        }
    }
}
