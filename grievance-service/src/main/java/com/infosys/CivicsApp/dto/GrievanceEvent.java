package com.infosys.CivicsApp.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class GrievanceEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType; // GRIEVANCE_CREATED, STATUS_UPDATED, OFFICER_ASSIGNED, OVERDUE_ESCALATED
    private UUID grievanceId;
    private UUID citizenId;
    private String citizenName;
    private String citizenEmail;
    private UUID officerId;
    private String officerName;
    private String officerEmail;
    private String title;
    private String status;
    private String remarks;
    private LocalDateTime timestamp;

    public GrievanceEvent() {
    }

    public GrievanceEvent(String eventType, UUID grievanceId, UUID citizenId, String citizenName, String citizenEmail,
                          UUID officerId, String officerName, String officerEmail, String title, String status,
                          String remarks, LocalDateTime timestamp) {
        this.eventType = eventType;
        this.grievanceId = grievanceId;
        this.citizenId = citizenId;
        this.citizenName = citizenName;
        this.citizenEmail = citizenEmail;
        this.officerId = officerId;
        this.officerName = officerName;
        this.officerEmail = officerEmail;
        this.title = title;
        this.status = status;
        this.remarks = remarks;
        this.timestamp = timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getGrievanceId() {
        return grievanceId;
    }

    public void setGrievanceId(UUID grievanceId) {
        this.grievanceId = grievanceId;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public String getCitizenEmail() {
        return citizenEmail;
    }

    public void setCitizenEmail(String citizenEmail) {
        this.citizenEmail = citizenEmail;
    }

    public UUID getOfficerId() {
        return officerId;
    }

    public void setOfficerId(UUID officerId) {
        this.officerId = officerId;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getOfficerEmail() {
        return officerEmail;
    }

    public void setOfficerEmail(String officerEmail) {
        this.officerEmail = officerEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GrievanceEvent{" +
                "eventType='" + eventType + '\'' +
                ", grievanceId=" + grievanceId +
                ", citizenId=" + citizenId +
                ", citizenName='" + citizenName + '\'' +
                ", citizenEmail='" + citizenEmail + '\'' +
                ", officerId=" + officerId +
                ", officerName='" + officerName + '\'' +
                ", officerEmail='" + officerEmail + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", remarks='" + remarks + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
