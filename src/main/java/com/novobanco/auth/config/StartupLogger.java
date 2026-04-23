package com.novobanco.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;
    private final String serverPort;

    public StartupLogger(Environment environment, @Value("${server.port}") String serverPort) {
        this.environment = environment;
        this.serverPort = serverPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupBanner() {
        String profile = resolveProfileLabel();
        String baseUrl = "http://localhost:" + serverPort;

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║     NovoBanco — Auth Service iniciado               ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Ambiente   : {}║", padRight(profile, 41));
        log.info("║  Puerto     : {}║", padRight(serverPort, 41));
        log.info("║  Swagger UI : {}║", padRight(baseUrl + "/swagger-ui.html", 41));
        log.info("║  API Base   : {}║", padRight(baseUrl + "/api/v1/auth", 41));
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    private String resolveProfileLabel() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "DEFAULT (desarrollo local)";
        }
        return String.join(", ", activeProfiles).toUpperCase();
    }

    private String padRight(String value, int width) {
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }
}
