package com.example;

import com.example.api.ExchangeRateApiClient;
import com.example.config.ApiConfig;
import com.example.service.ConversorService;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final int OPCION_SALIR = 8;

    public static void main(String[] args) {
        String apiKey = ApiConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("No se encontró la API key. Configura la variable de entorno EXCHANGE_RATE_API_KEY");
            System.err.println("o crea un archivo config.properties con: exchange.rate.api.key=TU_API_KEY");
            System.exit(1);
        }

        ExchangeRateApiClient apiClient = new ExchangeRateApiClient(apiKey);
        ConversorService conversor = new ConversorService(apiClient);
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("es", "AR"));
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        System.out.println("****************************************");
        System.out.println("   Bienvenido al Conversor de Monedas   ");
        System.out.println("****************************************");

        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do {
                mostrarMenu();
                opcion = leerOpcion(scanner);
                if (opcion == OPCION_SALIR) {
                    System.out.println("Hasta luego.");
                    break;
                }
                if (opcion >= 1 && opcion <= 7) {
                    double cantidad = leerCantidad(scanner);
                    if (cantidad > 0) {
                        ejecutarConversion(conversor, opcion, cantidad, numberFormat);
                    } else {
                        System.out.println("Por favor ingresa un valor numérico positivo.");
                    }
                } else {
                    System.out.println("Opción no válida. Elige un número del 1 al 8.");
                }
            } while (opcion != OPCION_SALIR);
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("1) Dólares estadounidenses (USD) → Pesos argentinos (ARS)");
        System.out.println("2) Pesos argentinos (ARS) → Dólares estadounidenses (USD)");
        System.out.println("3) Dólares estadounidenses (USD) → Reales brasileños (BRL)");
        System.out.println("4) Reales brasileños (BRL) → Dólares estadounidenses (USD)");
        System.out.println("5) Dólares estadounidenses (USD) → Pesos colombianos (COP)");
        System.out.println("6) Pesos colombianos (COP) → Dólares estadounidenses (USD)");
        System.out.println("7) Dólares estadounidenses (USD) → Bolivianos (BOB)");
        System.out.println("8) Salir");
        System.out.print("Elige una opción (1-8): ");
    }

    private static int leerOpcion(Scanner scanner) {
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        scanner.next();
        return -1;
    }

    private static double leerCantidad(Scanner scanner) {
        System.out.print("Ingresa el valor que deseas convertir: ");
        if (scanner.hasNextDouble()) {
            return scanner.nextDouble();
        }
        scanner.next();
        return -1;
    }

    private static void ejecutarConversion(ConversorService conversor, int opcion, double cantidad,
                                           NumberFormat numberFormat) {
        String origen;
        String destino;
        switch (opcion) {
            case 1 -> { origen = "USD"; destino = "ARS"; }
            case 2 -> { origen = "ARS"; destino = "USD"; }
            case 3 -> { origen = "USD"; destino = "BRL"; }
            case 4 -> { origen = "BRL"; destino = "USD"; }
            case 5 -> { origen = "USD"; destino = "COP"; }
            case 6 -> { origen = "COP"; destino = "USD"; }
            case 7 -> { origen = "USD"; destino = "BOB"; }
            default -> { return; }
        }

        try {
            ConversorService.ResultadoConversion resultado = conversor.convertir(origen, destino, cantidad);
            String cantidadStr = numberFormat.format(resultado.getCantidadOriginal());
            String valorFinalStr = numberFormat.format(resultado.getValorFinal());
            System.out.printf("El valor de %s %s corresponde al valor final de %s %s.%n",
                    cantidadStr, resultado.getNombreOrigen(),
                    valorFinalStr, resultado.getNombreDestino());
        } catch (ExchangeRateApiClient.ExchangeRateApiException e) {
            String mensaje = mensajeErrorApi(e.getErrorType());
            System.out.println("Error de la API: " + mensaje);
        } catch (IOException e) {
            System.out.println("Error de conexión. Verifica tu internet e intenta de nuevo.");
        }
    }

    private static String mensajeErrorApi(String errorType) {
        return switch (errorType) {
            case "quota-reached" -> "Se ha alcanzado el límite de solicitudes del plan.";
            case "inactive-account" -> "La cuenta no está activa (confirma tu email).";
            case "invalid-key" -> "La API key no es válida.";
            case "malformed-request" -> "La solicitud no es correcta.";
            case "unsupported-code" -> "Código de moneda no soportado.";
            default -> errorType;
        };
    }
}
