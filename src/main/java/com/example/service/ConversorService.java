package com.example.service;

import com.example.api.ExchangeRateApiClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de conversión de monedas que delega en ExchangeRateApiClient
 * y proporciona nombres de monedas para los mensajes al usuario.
 */
public class ConversorService {

    private final ExchangeRateApiClient apiClient;

    private static final Map<String, String> NOMBRES_MONEDAS = new HashMap<>();

    static {
        NOMBRES_MONEDAS.put("USD", "dólares estadounidenses");
        NOMBRES_MONEDAS.put("ARS", "pesos argentinos");
        NOMBRES_MONEDAS.put("BRL", "reales brasileños");
        NOMBRES_MONEDAS.put("COP", "pesos colombianos");
        NOMBRES_MONEDAS.put("EUR", "euros");
        NOMBRES_MONEDAS.put("GBP", "libras esterlinas");
        NOMBRES_MONEDAS.put("MXN", "pesos mexicanos");
        NOMBRES_MONEDAS.put("CLP", "pesos chilenos");
        NOMBRES_MONEDAS.put("BOB", "bolivianos");
    }

    public ConversorService(ExchangeRateApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Convierte una cantidad de la moneda origen a la moneda destino.
     *
     * @param codigoOrigen  Código ISO 4217 de la moneda origen (ej. USD).
     * @param codigoDestino Código ISO 4217 de la moneda destino (ej. ARS).
     * @param cantidad     Cantidad a convertir.
     * @return Resultado de la conversión.
     */
    public ResultadoConversion convertir(String codigoOrigen, String codigoDestino, double cantidad)
            throws IOException, ExchangeRateApiClient.ExchangeRateApiException {

        ExchangeRateApiClient.ConversionResult result = apiClient.convert(
                codigoOrigen.toUpperCase(),
                codigoDestino.toUpperCase(),
                cantidad
        );

        String nombreOrigen = NOMBRES_MONEDAS.getOrDefault(result.getBaseCode(), result.getBaseCode());
        String nombreDestino = NOMBRES_MONEDAS.getOrDefault(result.getTargetCode(), result.getTargetCode());

        return new ResultadoConversion(
                result.getBaseCode(),
                result.getTargetCode(),
                nombreOrigen,
                nombreDestino,
                cantidad,
                result.getConversionRate(),
                result.getConversionResult()
        );
    }

    /**
     * Resultado listo para mostrar al usuario (con nombres de monedas).
     */
    public static class ResultadoConversion {
        private final String codigoOrigen;
        private final String codigoDestino;
        private final String nombreOrigen;
        private final String nombreDestino;
        private final double cantidadOriginal;
        private final double tasa;
        private final double valorFinal;

        public ResultadoConversion(String codigoOrigen, String codigoDestino,
                                   String nombreOrigen, String nombreDestino,
                                   double cantidadOriginal, double tasa, double valorFinal) {
            this.codigoOrigen = codigoOrigen;
            this.codigoDestino = codigoDestino;
            this.nombreOrigen = nombreOrigen;
            this.nombreDestino = nombreDestino;
            this.cantidadOriginal = cantidadOriginal;
            this.tasa = tasa;
            this.valorFinal = valorFinal;
        }

        public String getNombreOrigen() { return nombreOrigen; }
        public String getNombreDestino() { return nombreDestino; }
        public double getCantidadOriginal() { return cantidadOriginal; }
        public double getValorFinal() { return valorFinal; }
    }
}
