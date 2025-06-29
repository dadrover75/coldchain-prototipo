package org.coldchain.simulator;

import java.time.Instant;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        String[] deviceIds = {"123", "124", "125", "126", "127", "128"};
        double[] temperatures = {7.0, 6.5, 8.0, 6.8, 7.2, 6.9};
        Random random = new Random();

        String ip = NetworkUtil.getHostIP();
        String broker = "tcp://" + ip + ":1883";
        String clientId = "simulator-multi";

        MqttClient client = null;

        try {
            client = new MqttClient(broker, clientId, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            client.connect(options);

            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < deviceIds.length; j++) {
                    double delta = -0.5 + random.nextDouble();
                    temperatures[j] += delta;
                    temperatures[j] = Math.max(2.0, Math.min(11.0, temperatures[j]));

                    String message = String.format(
                            "{\n  \"device_id\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"temperature\": %.2f\n}",
                            deviceIds[j],
                            Instant.now(),
                            temperatures[j]
                    );

                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(0);

                    try {
                        if (!client.isConnected()) {
                            System.out.println("Cliente desconectado. Reintentando conexión...");
                            client.connect(options);
                        }
                        client.publish("device/temperature", mqttMessage);
                        System.out.println("Publicado: " + message);
                    } catch (MqttException e) {
                        System.err.println("Error al publicar: " + e.getMessage());
                        // Opcional: esperar unos segundos antes de reintentar
                        Thread.sleep(2000);
                    }
                }
                Thread.sleep(3000);
            }
        } catch (MqttException e) {
            System.err.println("Fallo en conexión inicial: " + e.getMessage());
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                    client.close();
                } catch (MqttException e) {
                    System.err.println("Error al cerrar cliente MQTT: " + e.getMessage());
                }
            }
        }
    }
}
