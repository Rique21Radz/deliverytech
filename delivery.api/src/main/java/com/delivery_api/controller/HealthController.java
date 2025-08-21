package com.delivery_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
* Controller responsável pelos endpoints de monitoramento da aplicação.
* Demonstra o uso de recursos modernos do Java 21.
*/
@RestController
public class HealthController {

    private static final DateTimeFormatter FORMATTER =
    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

/**
* Endpoint para verificar o status da aplicação.
*/
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {

    // Usando Map.of() (Java 9+) para criar mapa imutável.
    Map<String, String> healthInfo = Map.of(
    "status", "UP",
    "timestamp", LocalDateTime.now().format(FORMATTER),
    "service", "Delivery API",
    "javaVersion", System.getProperty("java.version"),
    "springBootVersion", getClass().getPackage().getImplementationVersion() != null
    ? getClass().getPackage().getImplementationVersion() : "3.5.4",
    "environment", "VS Code"
    );
    return ResponseEntity.ok(healthInfo);
}

/**
* Endpoint com informações detalhadas da aplicação.
* Demonstra o uso de Records (Java 14+).
* @return AppInfo com dados da aplicação.
*/

@GetMapping("/info")
public ResponseEntity<AppInfo> info() {
    AppInfo appInfo = new AppInfo(
        "Delivery Tech API",
        "1.0",
        "Henrique Radzevicius Toledo",
        "JDK 21",
        "Spring Boot 3.5.4",
        LocalDateTime.now().format(FORMATTER),
        "API de delivery"
    );
    return ResponseEntity.ok(appInfo);
}

/**
* Record para demonstrar recurso do Java 14+ (disponível no JDK 21).
*/
public record AppInfo(
    String application,
    String version,
    String developer,
    String javaVersion,
    String framework,
    String timestamp,
    String description
) {

// Construtor compacto para validação (opcional).
public AppInfo {
    if (application == null || application.isBlank()) {
    throw new IllegalArgumentException("Applica􀆟on name cannot be null or blank");
    }
}
}

}