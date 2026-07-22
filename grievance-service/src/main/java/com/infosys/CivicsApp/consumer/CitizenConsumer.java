package com.infosys.CivicsApp.consumer;

import com.infosys.CivicsApp.dto.CitizenRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CitizenConsumer {

    @KafkaListener(topics = "citizen-events", groupId = "citizen-group")
    public void consumeCitizen(CitizenRegisteredEvent event) {
        System.out.println("Received CitizenRegisteredEvent: " + event);
        System.out.println("Citizen Id : " + event.getCitizenId());
        System.out.println("Name: " + event.getName());
        System.out.println("Ward: " + event.getWard());
    }
}
