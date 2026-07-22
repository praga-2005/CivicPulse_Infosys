package com.infosys.CivicsApp.dto;

public class ServiceApplicationEvent {
    private String eventType; // ApplicationSubmittedEvent, DocumentVerifiedEvent, CertificateApprovedEvent, CertificateGeneratedEvent
    private Long applicationId;
    private String applicationNumber;
    private String citizenId;

    public ServiceApplicationEvent() {}

    public ServiceApplicationEvent(String eventType, Long applicationId, String applicationNumber, String citizenId) {
        this.eventType = eventType;
        this.applicationId = applicationId;
        this.applicationNumber = applicationNumber;
        this.citizenId = citizenId;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
}
