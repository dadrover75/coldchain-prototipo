package org.coldchain.simulator;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtil {
    public static String getHostIP() {
        try {
            // Intenta acceder al servidor de metadata de GCP
            URL metadataUrl = new URL("http://metadata.google.internal/computeMetadata/v1/instance/network-interfaces/0/access-configs/0/external-ip");
            HttpURLConnection conn = (HttpURLConnection) metadataUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Metadata-Flavor", "Google");
            conn.setConnectTimeout(1000);  // rápido fallback
            conn.setReadTimeout(1000);

            try (InputStream stream = conn.getInputStream();
                 Scanner scanner = new Scanner(stream)) {
                String ip = scanner.nextLine().trim();
                System.out.println("🌐 Ejecutando en GCP. IP pública: " + ip);
                return ip;
            }
        } catch (Exception e) {
            System.out.println("💻 Ejecutando localmente. Usando localhost.");
            return "localhost";
        }
    }
}

