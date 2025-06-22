package com.coldchain.backend.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    private static final String BROKER_URI = "tcp://mqtt:1883"; // "mqtt" es el nombre del servicio en docker-compose
    private static final String CLIENT_ID = "backend-subscriber";

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URI, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            client.subscribe("device/temperature", (topic, msg) -> {
                String payload = new String(msg.getPayload());
                System.out.println("Mensaje recibido: " + payload);
                // Aquí podés agregar deserialización y lógica de negocio
            });

            System.out.println("Conectado a MQTT y suscripto a topic device/temperature");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
