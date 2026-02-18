package com.example.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Carga la API key de Exchange Rate API desde variable de entorno
 * (EXCHANGE_RATE_API_KEY) o desde config.properties en la raíz del proyecto.
 */
public class ApiConfig {

    private static final String ENV_VAR = "EXCHANGE_RATE_API_KEY";
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_KEY = "exchange.rate.api.key";

    /**
     * Obtiene la API key: primero variable de entorno, luego config.properties.
     *
     * @return La API key o null si no está configurada.
     */
    public static String getApiKey() {
        String key = System.getenv(ENV_VAR);
        if (key != null && !key.isBlank()) {
            return key.trim();
        }

        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(configPath));
                key = props.getProperty(CONFIG_KEY);
                if (key != null && !key.isBlank()) {
                    return key.trim();
                }
            } catch (IOException ignored) {
                // No config file or invalid format
            }
        }

        return null;
    }
}
