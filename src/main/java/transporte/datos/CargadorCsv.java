package transporte.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import transporte.arbol.ArbolNario;
import transporte.grafo.Grafo;
import transporte.modelo.Validacion;

/**
 * Lee los archivos CSV del sistema:
 * <ul>
 *   <li>abordajes.csv: {@code codigo,parada,zona,abordajes}</li>
 *   <li>tramos.csv: {@code linea,desde,hacia,minutos}</li>
 * </ul>
 * Cualquier dato mal formado lanza {@link IllegalArgumentException} indicando
 * el número de línea del archivo.
 */
public final class CargadorCsv {

    public static final String ENCABEZADO_ABORDAJES = "codigo,parada,zona,abordajes";
    public static final String ENCABEZADO_TRAMOS = "linea,desde,hacia,minutos";

    private CargadorCsv() {
    }

    /**
     * Registra en el árbol las zonas y paradas del archivo, dentro de la ciudad indicada
     * (el CSV no trae columna de ciudad). Si la ciudad aún no existe, se registra.
     */
    public static void cargarAbordajes(Path archivo, String ciudad, ArbolNario arbol) throws IOException {
        if (arbol == null) {
            throw new IllegalArgumentException("Debe indicar el árbol de paradas.");
        }
        Validacion.requerirTexto(ciudad, "la ciudad");
        try (BufferedReader lector = abrir(archivo)) {
            leerEncabezado(lector, archivo, ENCABEZADO_ABORDAJES);
            if (!arbol.existeCiudad(ciudad)) {
                arbol.registrarCiudad(ciudad);
            }
            int numeroLinea = 1;
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    String[] columnas = columnas(linea, 4);
                    int abordajes = entero(columnas[3], "abordajes");
                    String zona = columnas[2];
                    if (!arbol.existeZona(ciudad, zona)) {
                        arbol.registrarZona(ciudad, zona);
                    }
                    arbol.registrarParada(ciudad, zona, columnas[0], columnas[1], abordajes);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }
    }

    /** Construye el grafo no dirigido con las paradas y tramos del archivo. */
    public static Grafo cargarTramos(Path archivo) throws IOException {
        Grafo grafo = new Grafo();
        try (BufferedReader lector = abrir(archivo)) {
            leerEncabezado(lector, archivo, ENCABEZADO_TRAMOS);
            int numeroLinea = 1;
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    String[] columnas = columnas(linea, 4);
                    Validacion.requerirTexto(columnas[0], "el nombre de la línea");
                    int minutos = entero(columnas[3], "minutos");
                    if (!grafo.existeParada(columnas[1])) {
                        grafo.agregarParada(columnas[1]);
                    }
                    if (!grafo.existeParada(columnas[2])) {
                        grafo.agregarParada(columnas[2]);
                    }
                    grafo.agregarTramo(columnas[1], columnas[2], minutos);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }
        return grafo;
    }

    // ------------------------------------------------------------ privados

    private static BufferedReader abrir(Path archivo) throws IOException {
        if (archivo == null) {
            throw new IllegalArgumentException("Debe indicar el archivo a leer.");
        }
        return Files.newBufferedReader(archivo, StandardCharsets.UTF_8);
    }

    private static void leerEncabezado(BufferedReader lector, Path archivo, String esperado) throws IOException {
        String nombre = archivo.getFileName().toString();
        String primera = lector.readLine();
        if (primera == null) {
            throw new IllegalArgumentException("El archivo '" + nombre + "' está vacío.");
        }
        if (primera.startsWith("﻿")) {
            primera = primera.substring(1);
        }
        if (!primera.trim().equals(esperado)) {
            throw new IllegalArgumentException(
                    "El encabezado del archivo '" + nombre + "' no es el esperado. Debe ser: " + esperado);
        }
    }

    private static String[] columnas(String linea, int esperadas) {
        String[] partes = linea.split(",", -1);
        if (partes.length != esperadas) {
            throw new IllegalArgumentException(
                    "se esperaban " + esperadas + " columnas y se encontraron " + partes.length + ".");
        }
        for (int i = 0; i < partes.length; i++) {
            partes[i] = partes[i].trim();
        }
        return partes;
    }

    private static int entero(String texto, String columna) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "el valor de '" + columna + "' debe ser un número entero, y se encontró '" + texto + "'.");
        }
    }
}
