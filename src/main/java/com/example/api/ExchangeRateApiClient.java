package com.example.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente para la API Exchange Rate v6 (Pair Conversion).
 * Documentación: https://www.exchangerate-api.com/docs/pair-conversion-requests
 */
public class ExchangeRateApiClient {

    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/%s/pair/%s/%s/%s";

    private final String apiKey;

    public ExchangeRateApiClient(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("La API key no puede ser nula ni vacía.");
        }
        this.apiKey = apiKey.trim();
    }

    /**
     * Convierte una cantidad de la moneda base a la moneda objetivo.
     *
     * @param baseCode   Código ISO 4217 de la moneda origen (ej. USD).
     * @param targetCode Código ISO 4217 de la moneda destino (ej. ARS).
     * @param amount     Cantidad a convertir (decimal).
     * @return Resultado de la conversión con tasa y valor final.
     * @throws IOException              Si hay error de red o lectura.
     * @throws ExchangeRateApiException Si la API devuelve result: "error".
     */
    public ConversionResult convert(String baseCode, String targetCode, double amount)
            throws IOException, ExchangeRateApiException {

        String urlStr = String.format(BASE_URL, apiKey, baseCode, targetCode, amount);
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error HTTP: " + responseCode);
        }

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonObject json = root.getAsJsonObject();
            String result = json.get("result").getAsString();

            if ("error".equals(result)) {
                String errorType = json.has("error-type") ? json.get("error-type").getAsString() : "unknown";
                throw new ExchangeRateApiException(errorType);
            }

            String base = json.get("base_code").getAsString();
            String target = json.get("target_code").getAsString();
            double rate = json.get("conversion_rate").getAsDouble();
            double conversionResult = json.get("conversion_result").getAsDouble();

            return new ConversionResult(base, target, rate, conversionResult);
        }
    }

    /**
     * Resultado de una conversión exitosa.
     */
    public static class ConversionResult {
        private final String baseCode;
        private final String targetCode;
        private final double conversionRate;
        private final double conversionResult;

        public ConversionResult(String baseCode, String targetCode, double conversionRate, double conversionResult) {
            this.baseCode = baseCode;
            this.targetCode = targetCode;
            this.conversionRate = conversionRate;
            this.conversionResult = conversionResult;
        }

        public String getBaseCode() { return baseCode; }
        public String getTargetCode() { return targetCode; }
        public double getConversionRate() { return conversionRate; }
        public double getConversionResult() { return conversionResult; }
    }

    /**
     * Excepción cuando la API devuelve result: "error".
     */
    public static class ExchangeRateApiException extends Exception {
        private final String errorType;

        public ExchangeRateApiException(String errorType) {
            super("API error: " + errorType);
            this.errorType = errorType;
        }

        public String getErrorType() { return errorType; }
    }
}
