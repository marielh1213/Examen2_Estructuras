package transporte;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.CIUDAD;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import transporte.abordajes.ContadorAbordajes;
import transporte.abordajes.ListadoAbordajes;
import transporte.abordajes.ReporteAbordajes;
import transporte.arbol.ArbolNario;
import transporte.grafo.Dijkstra;
import transporte.grafo.Grafo;
import transporte.grafo.RutaNoEncontradaException;
import transporte.ordenamiento.QuickSort;
import transporte.ordenamiento.RadixSort;

@DisplayName("RNF1 y RNF2 - Orden del reporte y lenguaje claro para el usuario")
class RequisitosNoFuncionalesTest {

    // ------------------------------------------------------------ utilidades para leer el reporte

    /** Líneas numeradas de la sección "Listado de paradas:" del reporte. */
    private String[] lineasDelListado(String reporte) {
        String[] lineas = reporte.split("\n");
        int inicio = -1;
        for (int i = 0; i < lineas.length; i++) {
            if (lineas[i].equals("Listado de paradas:")) {
                inicio = i + 1;
            }
        }
        assertTrue(inicio > 0, "el reporte no tiene la sección 'Listado de paradas:'");
        int fin = inicio;
        while (fin < lineas.length && !lineas[fin].isEmpty()) {
            fin++;
        }
        String[] listado = new String[fin - inicio];
        System.arraycopy(lineas, inicio, listado, 0, listado.length);
        return listado;
    }

    private String codigoDe(String linea) {
        return linea.substring(linea.indexOf(". ") + 2, linea.indexOf(" - "));
    }

    private long abordajesDe(String linea) {
        int inicio = linea.lastIndexOf("): ") + 3;
        int fin = linea.indexOf(" abordaje", inicio);
        return Long.parseLong(linea.substring(inicio, fin));
    }

    /** Árbol de 1,200 paradas en el que la parada p tiene p % 100 abordajes (muchos empates). */
    private ArbolNario arbolConEmpates() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = (i + 1) % 100;
        }
        return DatosDePrueba.arbolConAbordajes(abordajes);
    }

    // ------------------------------------------------------------ RNF1: el reporte muestra el orden pedido

    @Test
    @DisplayName("RNF1: el reporte del CSV muestra los abordajes en orden descendente (valores exactos)")
    void reporteDescendenteDelCsv() throws IOException {
        String[] listado = lineasDelListado(ReporteAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD));

        assertEquals(8, listado.length);
        long[] esperados = {1520, 1310, 1310, 980, 980, 640, 640, 455};
        String[] codigos = {"S01", "S03", "S08", "S02", "S05", "S04", "S07", "S06"};
        for (int i = 0; i < listado.length; i++) {
            assertTrue(listado[i].startsWith((i + 1) + ". "), "numeración de la línea " + (i + 1));
            assertEquals(esperados[i], abordajesDe(listado[i]));
            assertEquals(codigos[i], codigoDe(listado[i]));
        }
    }

    @Test
    @DisplayName("RNF1: con 1,200 paradas el reporte baja siempre de abordajes y desempata por código")
    void reporteDescendenteMilDoscientas() {
        String[] listado = lineasDelListado(ReporteAbordajes.generar(arbolConEmpates(), CIUDAD));

        assertEquals(1200, listado.length);
        for (int i = 1; i < listado.length; i++) {
            long anterior = abordajesDe(listado[i - 1]);
            long actual = abordajesDe(listado[i]);
            assertTrue(anterior >= actual, "no desciende en la línea " + (i + 1));
            if (anterior == actual) {
                assertTrue(codigoDe(listado[i - 1]).compareTo(codigoDe(listado[i])) < 0,
                        "empate fuera de orden por código en la línea " + (i + 1));
            }
        }
    }

    @Test
    @DisplayName("RNF1: el reporte dice con qué criterio está ordenado el listado")
    void elReporteIndicaElOrden() throws IOException {
        String reporte = ReporteAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD);
        assertTrue(reporte.contains(
                "Orden del listado: de mayor a menor abordaje; si hay empate, por código de parada de la A a la Z.\n"));
    }

    // ------------------------------------------------------------ RNF2: lenguaje claro y sencillo

    @Test
    @DisplayName("RNF2: la ruta se explica en una frase sencilla (texto exacto)")
    void rutaEnLenguajeSencillo() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertEquals("Ruta más corta: Terminal -> Estadio -> Parque -> Faro. Tiempo estimado: 15 minutos.",
                Dijkstra.rutaMasCorta(grafo, "Terminal", "Faro").descripcion());
        assertEquals("Ruta más corta: Terminal -> Playa. Tiempo estimado: 12 minutos.",
                Dijkstra.rutaMasCorta(grafo, "Terminal", "Playa").descripcion());
    }

    @Test
    @DisplayName("RNF2: si ya está en la parada, lo dice así (0 minutos)")
    void rutaAlMismoLugar() throws IOException {
        assertEquals("Ya se encuentra en la parada Terminal. Tiempo estimado: 0 minutos.",
                Dijkstra.rutaMasCorta(DatosDePrueba.grafoDesdeCsv(), "Terminal", "Terminal").descripcion());
    }

    @Test
    @DisplayName("RNF2: 1 minuto se escribe en singular")
    void unMinutoEnSingular() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        grafo.agregarTramo("A", "B", 1);
        assertEquals("Ruta más corta: A -> B. Tiempo estimado: 1 minuto.",
                Dijkstra.rutaMasCorta(grafo, "A", "B").descripcion());
    }

    @Test
    @DisplayName("RNF2: los mensajes de error están en español sencillo, sin términos técnicos")
    void erroresEnLenguajeSencillo() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        Grafo aislado = new Grafo();
        aislado.agregarParada("A");
        aislado.agregarParada("B");

        Executable[] accionesConError = {
            () -> arbol.registrarCiudad(""),
            () -> arbol.registrarCiudad(CIUDAD),
            () -> arbol.registrarZona("Nada", "Sur"),
            () -> arbol.registrarParada(CIUDAD, "Sur", "S99", "Nueva", 1),
            () -> arbol.registrarParada(CIUDAD, "Centro", "S01", "Repetida", 1),
            () -> arbol.registrarParada(CIUDAD, "Centro", "S99", "Nueva", -1),
            () -> arbol.obtenerParada("S99"),
            () -> ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Sur"),
            () -> ListadoAbordajes.generar(arbol, "Nada"),
            () -> ReporteAbordajes.generar(arbol, "Nada"),
            () -> RadixSort.ordenar(new int[] {-1}),
            () -> QuickSort.ordenar(null, null),
            () -> Dijkstra.rutaMasCorta(grafo, "Zoo", "Faro"),
            () -> Dijkstra.rutaMasCorta(grafo, "Terminal", ""),
            () -> grafo.agregarTramo("Terminal", "Faro", 0),
        };
        for (int i = 0; i < accionesConError.length; i++) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, accionesConError[i]);
            assertEsClaro(error.getMessage(), "acción " + i);
        }

        RutaNoEncontradaException sinRuta =
                assertThrows(RutaNoEncontradaException.class, () -> Dijkstra.rutaMasCorta(aislado, "A", "B"));
        assertEsClaro(sinRuta.getMessage(), "ruta inexistente");
        assertEquals("No hay una ruta para ir de 'A' a 'B'.", sinRuta.getMessage());
    }

    @Test
    @DisplayName("RNF2: el reporte es texto plano legible, sin referencias técnicas de objetos")
    void reporteLegible() throws IOException {
        String reporte = ReporteAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD);
        assertFalse(reporte.contains("@"));
        assertFalse(reporte.contains("Parada["));
        assertFalse(reporte.contains("null"));
        assertFalse(reporte.contains("Exception"));
        assertTrue(reporte.startsWith("REPORTE DE ABORDAJES DIARIOS\n"));
        assertTrue(reporte.contains("Abordajes por zona:\n"));
        assertTrue(reporte.contains("Listado de paradas:\n"));
        assertArrayEquals(new String[] {"S01", "S03"},
                new String[] {codigoDe(lineasDelListado(reporte)[0]), codigoDe(lineasDelListado(reporte)[1])});
    }

    private void assertEsClaro(String mensaje, String contexto) {
        assertTrue(mensaje != null && !mensaje.isBlank(), contexto + ": el mensaje está vacío");
        assertTrue(mensaje.endsWith("."), contexto + ": el mensaje debe terminar en punto: " + mensaje);
        String[] prohibidas = {"Exception", "null", "java", "transporte.", "Array", "index", "Index"};
        for (int i = 0; i < prohibidas.length; i++) {
            assertFalse(mensaje.contains(prohibidas[i]),
                    contexto + ": el mensaje tiene un término técnico (" + prohibidas[i] + "): " + mensaje);
        }
    }
}
