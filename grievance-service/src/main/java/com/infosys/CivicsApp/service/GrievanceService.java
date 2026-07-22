package com.infosys.CivicsApp.service;

import com.infosys.CivicsApp.client.CitizenClient;
import com.infosys.CivicsApp.client.DepartmentClient;
import com.infosys.CivicsApp.client.OfficerClient;
import com.infosys.CivicsApp.dto.CitizenDto;
import com.infosys.CivicsApp.dto.DepartmentDto;
import com.infosys.CivicsApp.dto.OfficerDto;
import com.infosys.CivicsApp.entity.Grievance;
import com.infosys.CivicsApp.entity.GrievanceHistory;
import com.infosys.CivicsApp.repository.GrievanceHistoryRepository;
import com.infosys.CivicsApp.repository.GrievanceRepository;
import com.infosys.CivicsApp.dto.GrievanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GrievanceService {

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private GrievanceHistoryRepository grievanceHistoryRepository;

    @Autowired
    private CitizenClient citizenClient;

    @Autowired
    private DepartmentClient departmentClient;

    @Autowired
    private OfficerClient officerClient;

    @Autowired(required = false)
    private KafkaTemplate<String, GrievanceEvent> kafkaTemplate;

    @Transactional
    public Grievance createGrievance(UUID citizenId, UUID departmentId, String title, String description, String location, String priority) {
        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            throw new RuntimeException("Title and Description are required!");
        }

        // Validate via Feign clients
        CitizenDto citizen = null;
        try {
            citizen = citizenClient.getCitizenById(citizenId);
        } catch (Exception e) {
            System.err.println("Citizen not found in local DB: " + e.getMessage());
        }
        String citizenName = (citizen != null && citizen.getName() != null) ? citizen.getName() : "Citizen (Keycloak Auth)";

        DepartmentDto department = departmentClient.getDepartmentById(departmentId);
        if (department == null) {
            throw new RuntimeException("Department not found!");
        }

        // Milestone 1: Prevent duplicate active grievances
        boolean duplicateExists = grievanceRepository.existsByCitizenIdAndTitleAndStatusNotIn(
                citizenId, title, Arrays.asList("RESOLVED", "CLOSED"));
        if (duplicateExists) {
            throw new RuntimeException("An active grievance with the same title already exists.");
        }

        Grievance grievance = new Grievance();
        grievance.setCitizenId(citizenId);
        grievance.setDepartmentId(departmentId);
        grievance.setTitle(title);
        grievance.setDescription(description);
        grievance.setLocation(location);
        
        if (priority == null || priority.isEmpty()) {
            priority = "MEDIUM";
        }
        grievance.setPriority(priority.toUpperCase());
        
        LocalDateTime now = LocalDateTime.now();
        grievance.setCreatedDate(now);
        grievance.setUpdatedDate(now);

        if ("HIGH".equals(grievance.getPriority())) {
            grievance.setSlaDeadline(now.plusDays(1));
        } else if ("LOW".equals(grievance.getPriority())) {
            grievance.setSlaDeadline(now.plusDays(7));
        } else {
            grievance.setSlaDeadline(now.plusDays(3));
        }

        // Auto assign to first officer of department
        try {
            List<OfficerDto> deptOfficers = officerClient.getOfficersByDepartment(departmentId);
            if (deptOfficers != null && !deptOfficers.isEmpty()) {
                OfficerDto assigned = deptOfficers.get(0);
                grievance.setAssignedOfficerId(assigned.getId());
                grievance.setStatus("ASSIGNED");
            } else {
                grievance.setStatus("NEW");
            }
        } catch (Exception e) {
            System.err.println("Could not auto-assign officer: " + e.getMessage());
            grievance.setStatus("NEW");
        }

        Grievance savedGrievance = grievanceRepository.save(grievance);

        // Log history
        logHistory(savedGrievance, "CREATED", citizenName, "Grievance filed successfully.");
        
        // Enrich relationships for return payload
        populateGrievanceRelations(savedGrievance);
        
        if (savedGrievance.getAssignedOfficerId() != null && savedGrievance.getAssignedOfficer() != null) {
            logHistory(savedGrievance, "ASSIGNED", "System", "Automatically assigned to officer: " + savedGrievance.getAssignedOfficer().getName());
        }

        sendGrievanceEvent(savedGrievance, "GRIEVANCE_CREATED", "Grievance filed successfully.");

        return savedGrievance;
    }

    @Transactional
    public Grievance updateGrievanceStatus(UUID grievanceId, String newStatus, String updatedBy, String remarks) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new RuntimeException("Grievance not found!"));

        String oldStatus = grievance.getStatus();
        grievance.setStatus(newStatus.toUpperCase());
        grievance.setUpdatedDate(LocalDateTime.now());

        Grievance savedGrievance = grievanceRepository.save(grievance);

        logHistory(savedGrievance, "STATUS_UPDATE", updatedBy, 
                "Status updated from " + oldStatus + " to " + newStatus.toUpperCase() + ". Remarks: " + remarks);

        sendGrievanceEvent(savedGrievance, "STATUS_UPDATED", "Status updated from " + oldStatus + " to " + newStatus.toUpperCase() + ". Remarks: " + remarks);

        return populateGrievanceRelations(savedGrievance);
    }

    @Transactional
    public Grievance assignOfficer(UUID grievanceId, UUID officerId, String updatedBy) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new RuntimeException("Grievance not found!"));
        
        OfficerDto officer = officerClient.getOfficerById(officerId);
        if (officer == null) {
            throw new RuntimeException("Officer not found!");
        }

        grievance.setAssignedOfficerId(officerId);
        grievance.setStatus("ASSIGNED");
        grievance.setUpdatedDate(LocalDateTime.now());

        Grievance savedGrievance = grievanceRepository.save(grievance);

        logHistory(savedGrievance, "ASSIGNED", updatedBy, "Assigned to officer: " + officer.getName());
        sendGrievanceEvent(savedGrievance, "OFFICER_ASSIGNED", "Assigned to officer: " + officer.getName());
        return populateGrievanceRelations(savedGrievance);
    }

    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void escalateOverdueGrievances() {
        LocalDateTime now = LocalDateTime.now();
        List<String> resolvedClosed = Arrays.asList("RESOLVED", "CLOSED");
        List<Grievance> overdueList = grievanceRepository.findBySlaDeadlineBeforeAndStatusNotInAndEscalatedFalse(now, resolvedClosed);

        // Fetch admin officer once
        UUID adminId = null;
        try {
            List<OfficerDto> allOfficers = officerClient.getAllOfficers();
            if (allOfficers != null) {
                Optional<OfficerDto> adminOpt = allOfficers.stream()
                        .filter(o -> "ADMIN".equals(o.getRole()))
                        .findFirst();
                if (adminOpt.isPresent()) {
                    adminId = adminOpt.get().getId();
                }
            }
        } catch (Exception e) {
            System.err.println("Could not retrieve admin officer for escalation: " + e.getMessage());
        }

        for (Grievance grievance : overdueList) {
            grievance.setEscalated(true);
            grievance.setUpdatedDate(now);
            
            if (adminId != null) {
                grievance.setAssignedOfficerId(adminId);
            }

            grievanceRepository.save(grievance);

            logHistory(grievance, "ESCALATED", "SLA Monitor Cron", "Grievance breached SLA deadline (" + grievance.getSlaDeadline() + ") and has been automatically escalated to administrative level.");
            sendGrievanceEvent(grievance, "OVERDUE_ESCALATED", "Grievance breached SLA deadline and has been automatically escalated to administrative level.");
        }
    }

    public List<Grievance> getAllGrievances() {
        return grievanceRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::populateGrievanceRelations)
                .collect(Collectors.toList());
    }

    public List<Grievance> getGrievancesByCitizen(UUID citizenId) {
        return grievanceRepository.findByCitizenIdOrderByCreatedDateDesc(citizenId).stream()
                .map(this::populateGrievanceRelations)
                .collect(Collectors.toList());
    }

    public List<Grievance> getGrievancesByOfficer(UUID officerId) {
        return grievanceRepository.findByAssignedOfficerIdOrderByCreatedDateDesc(officerId).stream()
                .map(this::populateGrievanceRelations)
                .collect(Collectors.toList());
    }

    public List<Grievance> getGrievancesByDepartment(UUID departmentId) {
        return grievanceRepository.findByDepartmentIdOrderByCreatedDateDesc(departmentId).stream()
                .map(this::populateGrievanceRelations)
                .collect(Collectors.toList());
    }

    public Grievance getGrievanceById(UUID id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grievance not found!"));
        return populateGrievanceRelations(grievance);
    }

    public List<GrievanceHistory> getGrievanceHistory(UUID grievanceId) {
        return grievanceHistoryRepository.findByGrievanceIdOrderByTimestampAsc(grievanceId);
    }

    @Transactional
    public void deleteGrievance(UUID grievanceId) {
        Grievance grievance = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new RuntimeException("Grievance not found!"));
        
        List<GrievanceHistory> histories = grievanceHistoryRepository.findByGrievanceIdOrderByTimestampAsc(grievanceId);
        grievanceHistoryRepository.deleteAll(histories);
        grievanceRepository.delete(grievance);
    }

    public void logHistory(Grievance grievance, String action, String updatedBy, String remarks) {
        GrievanceHistory history = new GrievanceHistory();
        history.setGrievance(grievance);
        history.setAction(action);
        history.setUpdatedBy(updatedBy);
        history.setRemarks(remarks);
        history.setTimestamp(LocalDateTime.now());
        grievanceHistoryRepository.save(history);
    }

    public Grievance populateGrievanceRelations(Grievance g) {
        if (g == null) return null;
        
        if (g.getCitizenId() != null) {
            try {
                g.setCitizen(citizenClient.getCitizenById(g.getCitizenId()));
            } catch (Exception e) {
                System.err.println("Could not load citizen details for grievance: " + e.getMessage());
            }
        }
        if (g.getDepartmentId() != null) {
            try {
                g.setDepartment(departmentClient.getDepartmentById(g.getDepartmentId()));
            } catch (Exception e) {
                System.err.println("Could not load department details for grievance: " + e.getMessage());
            }
        }
        if (g.getAssignedOfficerId() != null) {
            try {
                g.setAssignedOfficer(officerClient.getOfficerById(g.getAssignedOfficerId()));
            } catch (Exception e) {
                System.err.println("Could not load officer details for grievance: " + e.getMessage());
            }
        }
        return g;
    }

    private void sendGrievanceEvent(Grievance grievance, String eventType, String remarks) {
        if (kafkaTemplate == null) {
            System.out.println("KafkaTemplate is not configured or disabled. Skipping Kafka event publish.");
            return;
        }
        try {
            populateGrievanceRelations(grievance);
            
            GrievanceEvent event = new GrievanceEvent();
            event.setEventType(eventType);
            event.setGrievanceId(grievance.getId());
            event.setCitizenId(grievance.getCitizenId());
            if (grievance.getCitizen() != null) {
                event.setCitizenName(grievance.getCitizen().getName());
                event.setCitizenEmail(grievance.getCitizen().getEmail());
            }
            event.setOfficerId(grievance.getAssignedOfficerId());
            if (grievance.getAssignedOfficer() != null) {
                event.setOfficerName(grievance.getAssignedOfficer().getName());
                event.setOfficerEmail(grievance.getAssignedOfficer().getEmail());
            }
            event.setTitle(grievance.getTitle());
            event.setStatus(grievance.getStatus());
            event.setRemarks(remarks);
            event.setTimestamp(LocalDateTime.now());
            
            kafkaTemplate.send("grievance-events", grievance.getId().toString(), event);
            System.out.println("Published event to Kafka: " + event);
        } catch (Exception e) {
            System.err.println("Failed to publish Kafka event: " + e.getMessage());
        }
    }
}
