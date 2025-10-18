package com.coldchain.backend.service;

import com.coldchain.backend.model.Reading;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.hyperledger.fabric.client.*;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.Identities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.stream.Stream;

@ConditionalOnProperty(value = "fabric.gateway.enabled", havingValue = "true", matchIfMissing = true)
@Service
public class FabricGatewayService {

    @Value("${fabric.channel.name}")
    private String channelName;
    @Value("${fabric.chaincode.name}")
    private String chaincodeName;
    @Value("${fabric.msp.id}")
    private String mspId;
    @Value("${fabric.peer.endpoint}")
    private String peerEndpoint;
    @Value("${fabric.peer.hostOverride}")
    private String hostOverride;
    @Value("${fabric.tls.caCertPath}")
    private String tlsCaCertPath;
    @Value("${fabric.user.certPath}")
    private String userCertPath;
    @Value("${fabric.user.keyDir}")
    private String userKeyDirectory;
    @Value("${fabric.gateway.discovery:true}")
    private boolean discovery; // se mantiene por si a futuro se usa, actualmente sin efecto

    private ManagedChannel channel;
    private Gateway gateway;
    private Network network;
    private Contract contract;

    @PostConstruct
    public void init() {
        try {
            channel = createChannel();
            gateway = createGateway();
            network = gateway.getNetwork(channelName);
            contract = network.getContract(chaincodeName);
            System.out.println("[FabricGatewayService] Conexión establecida con chaincode " + chaincodeName);
        } catch (Exception e) {
            System.err.println("[FabricGatewayService] Error inicializando Gateway: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (gateway != null) gateway.close();
            if (channel != null) channel.shutdownNow();
        } catch (Exception e) {
            System.err.println("[FabricGatewayService] Error cerrando recursos: " + e.getMessage());
        }
    }

    private ManagedChannel createChannel() throws SSLException {
        File tlsCertFile = new File(tlsCaCertPath);
        if (!tlsCertFile.exists()) {
            throw new IllegalStateException("Certificado TLS no encontrado: " + tlsCaCertPath);
        }
        return NettyChannelBuilder.forTarget(peerEndpoint)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCertFile).build())
                .overrideAuthority(hostOverride)
                .build();
    }

    private Gateway createGateway() throws IOException, CertificateException, InvalidKeyException {
        // Leer certificado X509
        Path certPath = Path.of(userCertPath);
        if (!Files.exists(certPath)) {
            throw new IllegalStateException("Certificado de usuario no encontrado: " + certPath);
        }
        Certificate certificate;
        try (Reader certReader = Files.newBufferedReader(certPath)) {
            certificate = Identities.readX509Certificate(certReader);
        }
        Identity identity = new X509Identity(mspId, (X509Certificate) certificate);

        // Encontrar llave privada en keystore (acepta .pem o _sk)
        Path keyDir = Path.of(userKeyDirectory);
        if (!Files.isDirectory(keyDir)) {
            throw new IllegalStateException("Directorio de llaves no válido: " + keyDir);
        }
        Path keyPath;
        try (Stream<Path> files = Files.list(keyDir)) {
            keyPath = files.filter(p -> {
                        String fn = p.getFileName().toString();
                        return fn.endsWith(".pem") || fn.endsWith("_sk") || fn.endsWith(".key");
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No se encontró llave privada (._sk / .pem / .key) en " + keyDir));
        }
        PrivateKey privateKey;
        try (Reader keyReader = Files.newBufferedReader(keyPath)) {
            privateKey = Identities.readPrivateKey(keyReader);
        }
        Signer signer = Signers.newPrivateKeySigner(privateKey);

        Gateway.Builder builder = Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(channel);
        if (discovery) {
            System.out.println("[FabricGatewayService] (INFO) Discovery flag está habilitado pero la API Java 1.5.0 no expone builder.discovery(); ignorado.");
        }
        return builder.connect();
    }

    public boolean isReady() {
        return contract != null;
    }

    public String validarLecturaEnLedger(Reading reading) {
        if (contract == null) {
            return "Contrato no inicializado";
        }
        String lecturaID = reading.getDeviceId();
        Instant ts = reading.getTimestamp() != null ? reading.getTimestamp() : Instant.now();
        String lecturaJson = buildLecturaJson(ts, reading.getTemperature());
        try {
            byte[] result = contract.submitTransaction("ValidarLectura", lecturaID, lecturaJson);
            String resString = new String(result);
            System.out.println("[FabricGatewayService] Resultado ValidarLectura: " + resString);
            return resString;
        } catch (Exception e) {
            System.err.println("[FabricGatewayService] Error en submit ValidarLectura: " + e.getMessage());
            return "Error invoking chaincode: " + e.getMessage();
        }
    }

    public String obtenerLectura(String lecturaID) {
        if (contract == null) return "Contrato no inicializado";
        try {
            byte[] result = contract.evaluateTransaction("GetLectura", lecturaID);
            return new String(result);
        } catch (Exception e) {
            return "Error evaluando GetLectura: " + e.getMessage();
        }
    }

    public String obtenerHistorialLectura(String lecturaID) {
        if (contract == null) return "Contrato no inicializado";
        try {
            byte[] result = contract.evaluateTransaction("GetHistorialLectura", lecturaID);
            return new String(result);
        } catch (Exception e) {
            return "Error evaluando historial: " + e.getMessage();
        }
    }

    public String obtenerEstadoContrato() {
        if (contract == null) return "Contrato no inicializado";
        try {
            byte[] result = contract.evaluateTransaction("GetEstadoContrato");
            return new String(result);
        } catch (Exception e) {
            return "Error evaluando estado: " + e.getMessage();
        }
    }

    public String resetEstadoContrato() {
        if (contract == null) return "Contrato no inicializado";
        try {
            byte[] result = contract.submitTransaction("ResetEstadoContrato");
            return new String(result);
        } catch (Exception e) {
            return "Error submit reset: " + e.getMessage();
        }
    }

    private String buildLecturaJson(Instant timestamp, double temperature) {
        return String.format("{\"timestamp\":\"%s\",\"temperature\":%s}", timestamp.toString(), temperature);
    }
}
