package com.infosys.CivicsApp.entity;

import com.infosys.CivicsApp.dto.CitizenDto;
import com.infosys.CivicsApp.dto.DepartmentDto;
import com.infosys.CivicsApp.dto.OfficerDto;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grievances")
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "citizen_id", nullable = false)
    private UUID citizenId;

    @Transient
    private CitizenDto citizen;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Transient
    private DepartmentDto department;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    private String location;
    private String priority; // LOW, MEDIUM, HIGH
    private String status;   // NEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "assigned_officer_id", nullable = true)
    private UUID assignedOfficerId;

    @Transient
    private OfficerDto assignedOfficer;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime slaDeadline;

    private boolean escalated = false;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public CitizenDto getCitizen() {
        return citizen;
    }

    public void setCitizen(CitizenDto citizen) {
        this.citizen = citizen;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public DepartmentDto getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDto department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(UUID assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public OfficerDto getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(OfficerDto assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }
}
