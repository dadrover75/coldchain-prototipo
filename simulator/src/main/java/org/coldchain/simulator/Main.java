package org.coldchain.simulator;


import java.time.Instant;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MqttException {
        String deviceId = "123";
        double temperature = 7.0; // temperatura inicial
        Random random = new Random();

        String broker = "tcp://localhost:1883";
        String clientId = "simulator123";

        MqttClient client = new MqttClient(broker, clientId, null);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);

        try {
            for (int i = 0; i < 20; i++) {
                // Variación suave: entre -0.5 y +0.5
                double delta = -0.5 + random.nextDouble();
                temperature += delta;
                temperature = Math.max(5.0, Math.min(10.0, temperature));

                String message = String.format(
                        "{\n  \"device_id\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"temperature\": %.2f\n}",
                        deviceId,
                        Instant.now(),
                        temperature
                );

                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                mqttMessage.setQos(0);
                client.publish("device/temperature", mqttMessage);

                System.out.println("Publicado: " + message);
                Thread.sleep(5000);
            }
        } finally {
            client.disconnect();
            client.close();
        }
    }
}
