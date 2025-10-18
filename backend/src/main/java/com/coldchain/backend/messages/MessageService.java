package com.coldchain.backend.messages;

import com.coldchain.backend.controller.ReadingWebSocketController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.coldchain.backend.service.TemperatureService;
import java.io.IOException;

import com.coldchain.backend.model.Reading;
import com.coldchain.backend.repository.ReadingRepository;
import com.coldchain.backend.service.FabricGatewayService;

@Service
public class MessageService {

    @Value("${mqtt.broker.uri}")
    private String BROKER_URI;
    private static final String CLIENT_ID = "backend-subscriber";

    private MqttClient client;
    private final ReadingRepository readingRepository;
    private final TemperatureService temperatureService;
    private final ReadingWebSocketController readingWebSocketController;
    @Autowired(required = false)
    private FabricGatewayService fabricGatewayService; // opcional sin @Nullable

    @Autowired
    public MessageService(ReadingRepository readingRepository, TemperatureService temperatureService, ReadingWebSocketController readingWebSocketController) {
        this.readingRepository = readingRepository;
        this.temperatureService = temperatureService;
        this.readingWebSocketController = readingWebSocketController;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient(BROKER_URI, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            client.subscribe("device/temperature", (topic, msg) -> {
                String payload = new String(msg.getPayload());
                System.out.println("Mensaje recibido: " + payload);

                try {
                    Reading reading = objectMapper.readValue(payload, Reading.class);

                    // Invocar chaincode mediante Fabric Gateway si está disponible
                    if (fabricGatewayService != null) {
                        String fabricResult = fabricGatewayService.validarLecturaEnLedger(reading);
                        System.out.println("Resultado invocación Fabric: " + fabricResult);
                    } else {
                        System.out.println("FabricGatewayService no disponible (deshabilitado en este perfil). Se omite envío a ledger.");
                    }

                    // Validar y setear status usando el servicio de negocio
                    String status = temperatureService.validarStatus(reading.getTemperature());
                    reading.setStatus(status);

                    readingRepository.save(reading);
                    // Enviar la lectura a través de WebSocket
                    readingWebSocketController.sendReading(reading);
                    System.out.println("Lectura guardada en base de datos: " + reading.getDeviceId() + " " + reading.getTemperature());
                } catch (IOException e) {
                    System.err.println("Error al convertir o guardar el mensaje: " + e.getMessage());
                    e.printStackTrace();
                }
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
