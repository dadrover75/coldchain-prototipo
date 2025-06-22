package com.coldchain.backend.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @Value("${mqtt.broker.uri}")
    private String BROKER_URI;
    //private static final String BROKER_URI = "tcp://localhost:1883"; // para probar hasta que funcione docker backend, usar localhost:1883
    //private static final String BROKER_URI = "tcp://mqtt:1883"; // "mqtt" es el nombre del servicio en docker-compose
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


                // Aquí puedes procesar el mensaje recibido, por ejemplo, guardarlo en una base de datos
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
