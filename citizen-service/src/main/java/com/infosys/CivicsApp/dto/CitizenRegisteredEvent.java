package com.infosys.CivicsApp.dto;

import java.io.Serializable;
import java.util.UUID;

public class CitizenRegisteredEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID citizenId;
    private String name;
    private String email;
    private String ward;

    public CitizenRegisteredEvent() {
    }

    public CitizenRegisteredEvent(UUID citizenId, String name, String email, String ward) {
        this.citizenId = citizenId;
        this.name = name;
        this.email = email;
        this.ward = ward;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    @Override
    public String toString() {
        return "CitizenRegisteredEvent{" +
                "citizenId=" + citizenId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", ward='" + ward + '\'' +
                '}';
    }
}
