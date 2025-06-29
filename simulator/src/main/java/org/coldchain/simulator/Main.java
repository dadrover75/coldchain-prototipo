package org.coldchain.simulator;

import java.time.Instant;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, MqttException {
        // Lista de IDs de dispositivos
        String[] deviceIds = {"123", "124", "125", "126", "127", "128"};
        double[] temperatures = {7.0, 6.5, 8.0, 6.8, 7.2, 6.9};  // valores iniciales por dispositivo
        Random random = new Random();

        String ip = NetworkUtil.getHostIP();

        String broker = "tcp://" + ip + ":1883";
        String clientId = "simulator-multi";

        MqttClient client = new MqttClient(broker, clientId, null);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);

        try {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < deviceIds.length; j++) {
                    // Variación suave entre -0.5 y +0.5
                    double delta = -0.5 + random.nextDouble();
                    temperatures[j] += delta;
                    // Limitar entre 2.0 y 11.0 para cubrir casos OK, warning y fail
                    temperatures[j] = Math.max(2.0, Math.min(11.0, temperatures[j]));

                    String message = String.format(
                            "{\n  \"device_id\": \"%s\",\n  \"timestamp\": \"%s\",\n  \"temperature\": %.2f\n}",
                            deviceIds[j],
                            Instant.now(),
                            temperatures[j]
                    );

                    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                    mqttMessage.setQos(0);
                    client.publish("device/temperature", mqttMessage);

                    System.out.println("Publicado: " + message);
                }
                Thread.sleep(3000); // espera 3s entre cada ronda de publicaciones
            }
        } finally {
            client.disconnect();
            client.close();
        }
    }
}
