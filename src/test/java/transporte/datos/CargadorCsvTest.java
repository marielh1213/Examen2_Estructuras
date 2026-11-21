package transporte.datos;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.CIUDAD;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import transporte.DatosDePrueba;
import transporte.arbol.ArbolNario;
import transporte.grafo.Grafo;
import transporte.modelo.Parada;

@DisplayName("Lectura de abordajes.csv y tramos.csv (datos de entrada de RF1, RF2 y RF3)")
class CargadorCsvTest {

    private static final String ENC_ABORDAJES = "codigo,parada,zona,abordajes\n";
    private static final String ENC_TRAMOS = "linea,desde,hacia,minutos\n";

    private Path escribir(Path carpeta, String nombre, String contenido) throws IOException {
        return Files.writeString(carpeta.resolve(nombre), contenido);
    }

    private ArbolNario cargarAbordajes(Path archivo) throws IOException {
        ArbolNario arbol = new ArbolNario();
        CargadorCsv.cargarAbordajes(archivo, CIUDAD, arbol);
        return arbol;
    }

    // ------------------------------------------------------------ valores exactos (archivos reales)

    @Test
    @DisplayName("abordajes.csv: 1 ciudad, 3 zonas y 8 paradas con sus valores exactos")
    void abordajesDelCsv() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();

        assertEquals(1, arbol.cantidadCiudades());
        assertArrayEquals(new String[] {"Centro", "Norte", "Costa"}, arbol.zonas(CIUDAD));
        assertEquals(3, arbol.cantidadParadas(CIUDAD, "Centro"));
        assertEquals(3, arbol.cantidadParadas(CIUDAD, "Norte"));
        assertEquals(2, arbol.cantidadParadas(CIUDAD, "Costa"));
        assertEquals(8, arbol.cantidadParadas());
        assertArrayEquals(new String[] {"S01", "S02", "S08", "S03", "S05", "S06", "S04", "S07"},
                DatosDePrueba.codigos(arbol.paradasDeCiudad(CIUDAD)));

        Parada faro = arbol.obtenerParada("S04");
        assertEquals("Faro", faro.getNombre());
        assertEquals("Costa", faro.getZona());
        assertEquals(640, faro.getAbordajes());
        assertEquals(1520, arbol.obtenerParada("S01").getAbordajes());
        assertEquals(455, arbol.obtenerParada("S06").getAbordajes());
    }

    @Test
    @DisplayName("tramos.csv: 8 paradas y 13 tramos")
    void tramosDelCsv() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertEquals(8, grafo.cantidadParadas());
        assertEquals(13, grafo.cantidadTramos());
        assertEquals(6, grafo.minutosDirectos("Terminal", "Mercado"));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: abordajes.csv con solo el encabezado registra la ciudad sin paradas")
    void abordajesSoloEncabezado(@TempDir Path carpeta) throws IOException {
        ArbolNario arbol = cargarAbordajes(escribir(carpeta, "abordajes.csv", ENC_ABORDAJES));
        assertTrue(arbol.existeCiudad(CIUDAD));
        assertEquals(0, arbol.cantidadZonas(CIUDAD));
        assertEquals(0, arbol.cantidadParadas());
    }

    @Test
    @DisplayName("Límite: tramos.csv con solo el encabezado da un grafo vacío")
    void tramosSoloEncabezado(@TempDir Path carpeta) throws IOException {
        Grafo grafo = CargadorCsv.cargarTramos(escribir(carpeta, "tramos.csv", ENC_TRAMOS));
        assertEquals(0, grafo.cantidadParadas());
        assertEquals(0, grafo.cantidadTramos());
    }

    @Test
    @DisplayName("Límite: se ignoran las líneas en blanco, los espacios alrededor de los datos y el BOM")
    void lineasEnBlancoEspaciosYBom(@TempDir Path carpeta) throws IOException {
        String contenido = "﻿" + ENC_ABORDAJES
                + "\n"
                + " S01 , Terminal , Centro , 100 \n"
                + "   \n"
                + "S02,Mercado,Centro,50\n";
        ArbolNario arbol = cargarAbordajes(escribir(carpeta, "abordajes.csv", contenido));

        assertEquals(2, arbol.cantidadParadas());
        assertEquals("Terminal", arbol.obtenerParada("S01").getNombre());
        assertEquals("Centro", arbol.obtenerParada("S01").getZona());
        assertEquals(100, arbol.obtenerParada("S01").getAbordajes());
    }

    @Test
    @DisplayName("Límite: una línea final sin salto de línea también se lee")
    void sinSaltoFinal(@TempDir Path carpeta) throws IOException {
        Grafo grafo = CargadorCsv.cargarTramos(escribir(carpeta, "tramos.csv", ENC_TRAMOS + "Bus 9,A,B,4"));
        assertEquals(1, grafo.cantidadTramos());
        assertEquals(4, grafo.minutosDirectos("B", "A"));
    }

    @Test
    @DisplayName("Límite: una parada con 0 abordajes es válida")
    void ceroAbordajes(@TempDir Path carpeta) throws IOException {
        ArbolNario arbol = cargarAbordajes(escribir(carpeta, "abordajes.csv", ENC_ABORDAJES + "S01,Terminal,Centro,0\n"));
        assertEquals(0, arbol.obtenerParada("S01").getAbordajes());
    }

    // ------------------------------------------------------------ entradas inválidas: abordajes.csv

    @Test
    @DisplayName("Inválido: archivo que no existe")
    void archivoInexistente(@TempDir Path carpeta) {
        Path falso = carpeta.resolve("no_existe.csv");
        assertThrows(IOException.class, () -> cargarAbordajes(falso));
        assertThrows(IOException.class, () -> CargadorCsv.cargarTramos(falso));
    }

    @Test
    @DisplayName("Inválido: archivo, ciudad o árbol nulos")
    void argumentosNulos(@TempDir Path carpeta) throws IOException {
        Path archivo = escribir(carpeta, "abordajes.csv", ENC_ABORDAJES);
        assertError("Debe indicar el archivo a leer.", () -> cargarAbordajes(null));
        assertError("Debe indicar el archivo a leer.", () -> CargadorCsv.cargarTramos(null));
        assertError("Debe indicar la ciudad.", () -> CargadorCsv.cargarAbordajes(archivo, null, new ArbolNario()));
        assertError("Debe indicar la ciudad.", () -> CargadorCsv.cargarAbordajes(archivo, " ", new ArbolNario()));
        assertError("Debe indicar el árbol de paradas.", () -> CargadorCsv.cargarAbordajes(archivo, CIUDAD, null));
    }

    @Test
    @DisplayName("Inválido: archivo completamente vacío")
    void archivoVacio(@TempDir Path carpeta) throws IOException {
        Path abordajes = escribir(carpeta, "abordajes.csv", "");
        Path tramos = escribir(carpeta, "tramos.csv", "");
        assertError("El archivo 'abordajes.csv' está vacío.", () -> cargarAbordajes(abordajes));
        assertError("El archivo 'tramos.csv' está vacío.", () -> CargadorCsv.cargarTramos(tramos));
    }

    @Test
    @DisplayName("Inválido: encabezado distinto del esperado")
    void encabezadoIncorrecto(@TempDir Path carpeta) throws IOException {
        Path abordajes = escribir(carpeta, "abordajes.csv", "codigo,parada,abordajes,zona\nS01,Terminal,10,Centro\n");
        Path tramos = escribir(carpeta, "tramos.csv", "linea,origen,destino,tiempo\n");
        assertError("El encabezado del archivo 'abordajes.csv' no es el esperado. Debe ser: codigo,parada,zona,abordajes",
                () -> cargarAbordajes(abordajes));
        assertError("El encabezado del archivo 'tramos.csv' no es el esperado. Debe ser: linea,desde,hacia,minutos",
                () -> CargadorCsv.cargarTramos(tramos));
    }

    @Test
    @DisplayName("Inválido: abordajes.csv con columnas de menos o de más")
    void abordajesColumnas(@TempDir Path carpeta) throws IOException {
        Path pocas = escribir(carpeta, "pocas.csv", ENC_ABORDAJES + "S01,Terminal,Centro\n");
        Path muchas = escribir(carpeta, "muchas.csv", ENC_ABORDAJES + "S01,Terminal,Centro,10,extra\n");
        assertError("Línea 2: se esperaban 4 columnas y se encontraron 3.", () -> cargarAbordajes(pocas));
        assertError("Línea 2: se esperaban 4 columnas y se encontraron 5.", () -> cargarAbordajes(muchas));
    }

    @Test
    @DisplayName("Inválido: abordajes que no es un número entero (texto, decimal o vacío)")
    void abordajesNoNumerico(@TempDir Path carpeta) throws IOException {
        Path texto = escribir(carpeta, "texto.csv", ENC_ABORDAJES + "S01,Terminal,Centro,10\nS02,Mercado,Centro,muchos\n");
        Path decimal = escribir(carpeta, "decimal.csv", ENC_ABORDAJES + "S01,Terminal,Centro,12.5\n");
        Path vacio = escribir(carpeta, "vacio.csv", ENC_ABORDAJES + "S01,Terminal,Centro,\n");
        assertError("Línea 3: el valor de 'abordajes' debe ser un número entero, y se encontró 'muchos'.",
                () -> cargarAbordajes(texto));
        assertError("Línea 2: el valor de 'abordajes' debe ser un número entero, y se encontró '12.5'.",
                () -> cargarAbordajes(decimal));
        assertError("Línea 2: el valor de 'abordajes' debe ser un número entero, y se encontró ''.",
                () -> cargarAbordajes(vacio));
    }

    @Test
    @DisplayName("Inválido: abordajes negativos")
    void abordajesNegativos(@TempDir Path carpeta) throws IOException {
        Path archivo = escribir(carpeta, "abordajes.csv", ENC_ABORDAJES + "S01,Terminal,Centro,-5\n");
        assertError("Línea 2: Los abordajes de la parada S01 no pueden ser negativos.", () -> cargarAbordajes(archivo));
    }

    @Test
    @DisplayName("Inválido: código repetido, código vacío o zona vacía")
    void abordajesCamposInvalidos(@TempDir Path carpeta) throws IOException {
        Path repetido = escribir(carpeta, "repetido.csv",
                ENC_ABORDAJES + "S01,Terminal,Centro,10\nS01,Mercado,Norte,20\n");
        Path sinCodigo = escribir(carpeta, "sincodigo.csv", ENC_ABORDAJES + ",Terminal,Centro,10\n");
        Path sinZona = escribir(carpeta, "sinzona.csv", ENC_ABORDAJES + "S01,Terminal,,10\n");
        assertError("Línea 3: Ya existe una parada con el código 'S01'.", () -> cargarAbordajes(repetido));
        assertError("Línea 2: Debe indicar el código de la parada.", () -> cargarAbordajes(sinCodigo));
        assertError("Línea 2: Debe indicar la zona.", () -> cargarAbordajes(sinZona));
    }

    @Test
    @DisplayName("Inválido: el número de línea del error cuenta también las líneas en blanco")
    void numeroDeLineaConBlancos(@TempDir Path carpeta) throws IOException {
        Path archivo = escribir(carpeta, "abordajes.csv", ENC_ABORDAJES + "S01,Terminal,Centro,10\n\n\nS02,Mercado,Centro,x\n");
        assertError("Línea 5: el valor de 'abordajes' debe ser un número entero, y se encontró 'x'.",
                () -> cargarAbordajes(archivo));
    }

    // ------------------------------------------------------------ entradas inválidas: tramos.csv

    @Test
    @DisplayName("Inválido: tramos.csv con columnas de menos o de más")
    void tramosColumnas(@TempDir Path carpeta) throws IOException {
        Path pocas = escribir(carpeta, "pocas.csv", ENC_TRAMOS + "Bus 1,A,B\n");
        Path muchas = escribir(carpeta, "muchas.csv", ENC_TRAMOS + "Bus 1,A,B,5,extra\n");
        assertError("Línea 2: se esperaban 4 columnas y se encontraron 3.", () -> CargadorCsv.cargarTramos(pocas));
        assertError("Línea 2: se esperaban 4 columnas y se encontraron 5.", () -> CargadorCsv.cargarTramos(muchas));
    }

    @Test
    @DisplayName("Inválido: minutos que no es un número entero")
    void tramosMinutosNoNumericos(@TempDir Path carpeta) throws IOException {
        Path texto = escribir(carpeta, "texto.csv", ENC_TRAMOS + "Bus 1,A,B,rapido\n");
        Path decimal = escribir(carpeta, "decimal.csv", ENC_TRAMOS + "Bus 1,A,B,2.5\n");
        assertError("Línea 2: el valor de 'minutos' debe ser un número entero, y se encontró 'rapido'.",
                () -> CargadorCsv.cargarTramos(texto));
        assertError("Línea 2: el valor de 'minutos' debe ser un número entero, y se encontró '2.5'.",
                () -> CargadorCsv.cargarTramos(decimal));
    }

    @Test
    @DisplayName("Inválido: minutos en cero o negativos")
    void tramosMinutosNoPositivos(@TempDir Path carpeta) throws IOException {
        Path cero = escribir(carpeta, "cero.csv", ENC_TRAMOS + "Bus 1,A,B,0\n");
        Path negativo = escribir(carpeta, "negativo.csv", ENC_TRAMOS + "Bus 1,A,B,-3\n");
        assertError("Línea 2: El tiempo del tramo entre 'A' y 'B' debe ser mayor que cero.",
                () -> CargadorCsv.cargarTramos(cero));
        assertError("Línea 2: El tiempo del tramo entre 'A' y 'B' debe ser mayor que cero.",
                () -> CargadorCsv.cargarTramos(negativo));
    }

    @Test
    @DisplayName("Inválido: línea sin nombre, parada vacía o tramo de una parada consigo misma")
    void tramosCamposInvalidos(@TempDir Path carpeta) throws IOException {
        Path sinLinea = escribir(carpeta, "sinlinea.csv", ENC_TRAMOS + ",A,B,5\n");
        Path sinDesde = escribir(carpeta, "sindesde.csv", ENC_TRAMOS + "Bus 1,,B,5\n");
        Path consigoMisma = escribir(carpeta, "consigo.csv", ENC_TRAMOS + "Bus 1,A,A,5\n");
        assertError("Línea 2: Debe indicar el nombre de la línea.", () -> CargadorCsv.cargarTramos(sinLinea));
        assertError("Línea 2: Debe indicar el nombre de la parada.", () -> CargadorCsv.cargarTramos(sinDesde));
        assertError("Línea 2: Un tramo no puede unir la parada 'A' consigo misma.",
                () -> CargadorCsv.cargarTramos(consigoMisma));
    }
}
