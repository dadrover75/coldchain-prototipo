package com.coldchain.backend.controller;

import com.coldchain.backend.model.Reading;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class ReadingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ReadingWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendReading(Reading reading) {
        messagingTemplate.convertAndSend("/topic/readings", reading);
    }
}
