package com.infosys.CivicsApp.listener;

import com.infosys.CivicsApp.dto.GrievanceEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GrievanceEventListener {

    @KafkaListener(topics = "grievance-events", groupId = "notification-group")
    public void handleGrievanceEvent(GrievanceEvent event) {
        System.out.println("====== [NOTIFICATION SERVICE] NEW EVENT RECEIVED ======");
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Grievance ID: " + event.getGrievanceId());
        System.out.println("Title: " + event.getTitle());
        System.out.println("Status: " + event.getStatus());
        System.out.println("Remarks: " + event.getRemarks());
        System.out.println("Timestamp: " + event.getTimestamp());

        // Simulate Notification Delivery
        if (event.getCitizenEmail() != null) {
            sendMockEmail(event.getCitizenEmail(), event.getCitizenName(), 
                "Grievance Status Update: " + event.getTitle(),
                String.format("Dear %s,\n\nYour grievance (ID: %s) titled '%s' has been updated to: %s.\nRemarks: %s\n\nBest regards,\nCivicPulse Team", 
                    event.getCitizenName(), event.getGrievanceId(), event.getTitle(), event.getStatus(), event.getRemarks())
            );
        }

        if (event.getOfficerEmail() != null && "OFFICER_ASSIGNED".equals(event.getEventType())) {
            sendMockEmail(event.getOfficerEmail(), event.getOfficerName(),
                "New Grievance Assigned: " + event.getTitle(),
                String.format("Dear Officer %s,\n\nYou have been assigned a new grievance (ID: %s) titled '%s'.\n\nPlease address it as soon as possible.\n\nBest regards,\nCivicPulse Admin System",
                    event.getOfficerName(), event.getGrievanceId(), event.getTitle())
            );
        }
        System.out.println("=====================================================");
    }

    private void sendMockEmail(String email, String recipientName, String subject, String body) {
        System.out.println(">>> [MOCK EMAIL SENT] >>>");
        System.out.println("To: " + recipientName + " <" + email + ">");
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("-----------------------------------------------------");
    }
}
