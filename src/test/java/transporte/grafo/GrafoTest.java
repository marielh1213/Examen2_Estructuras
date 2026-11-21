package transporte.grafo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;

@DisplayName("RF3 (req. 6) - Grafo no dirigido y ponderado construido con tramos.csv")
class GrafoTest {

    // ------------------------------------------------------------ valores exactos

    @Test
    @DisplayName("El grafo del CSV tiene 8 paradas y 13 tramos")
    void cantidades() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertEquals(8, grafo.cantidadParadas());
        assertEquals(13, grafo.cantidadTramos());
        for (int i = 0; i < DatosDePrueba.NOMBRES_PARADAS.length; i++) {
            assertTrue(grafo.existeParada(DatosDePrueba.NOMBRES_PARADAS[i]), DatosDePrueba.NOMBRES_PARADAS[i]);
        }
    }

    @Test
    @DisplayName("Minutos exactos de los tramos directos, en ambos sentidos")
    void minutosDirectos() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        // Bus 1
        assertEquals(6, grafo.minutosDirectos("Terminal", "Mercado"));
        assertEquals(6, grafo.minutosDirectos("Mercado", "Terminal"));
        assertEquals(7, grafo.minutosDirectos("Mercado", "Universidad"));
        assertEquals(9, grafo.minutosDirectos("Universidad", "Faro"));
        // Bus 2
        assertEquals(5, grafo.minutosDirectos("Terminal", "Estadio"));
        assertEquals(4, grafo.minutosDirectos("Estadio", "Parque"));
        assertEquals(6, grafo.minutosDirectos("Parque", "Faro"));
        assertEquals(5, grafo.minutosDirectos("Faro", "Playa"));
        assertEquals(12, grafo.minutosDirectos("Playa", "Terminal"));
        // Bus 3
        assertEquals(4, grafo.minutosDirectos("Mercado", "Hospital"));
        assertEquals(3, grafo.minutosDirectos("Hospital", "Universidad"));
        assertEquals(15, grafo.minutosDirectos("Universidad", "Playa"));
        // Bus 4 y caminata
        assertEquals(2, grafo.minutosDirectos("Parque", "Universidad"));
        assertEquals(2, grafo.minutosDirectos("Universidad", "Parque"));
        assertEquals(10, grafo.minutosDirectos("Mercado", "Parque"));
        assertEquals(10, grafo.minutosDirectos("Parque", "Mercado"));
    }

    @Test
    @DisplayName("Si no hay tramo directo, minutosDirectos devuelve -1")
    void sinTramoDirecto() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertEquals(-1, grafo.minutosDirectos("Terminal", "Faro"));
        assertEquals(-1, grafo.minutosDirectos("Terminal", "Zoo"));
        assertEquals(-1, grafo.minutosDirectos(null, "Faro"));
        assertFalse(grafo.existeParada("Zoo"));
        assertFalse(grafo.existeParada(null));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: grafo vacío")
    void grafoVacio() {
        Grafo grafo = new Grafo();
        assertEquals(0, grafo.cantidadParadas());
        assertEquals(0, grafo.cantidadTramos());
        assertError("La parada 'A' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "A", "A"));
    }

    @Test
    @DisplayName("Límite: dos tramos entre las mismas paradas se cuentan por separado")
    void tramosRepetidos() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        grafo.agregarTramo("A", "B", 5);
        grafo.agregarTramo("B", "A", 3);
        assertEquals(2, grafo.cantidadTramos());
        assertEquals(3, grafo.minutosDirectos("A", "B"));
    }

    @Test
    @DisplayName("Límite: una parada con muchos tramos (más de los que caben al inicio)")
    void paradaConMuchosTramos() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("Centro");
        for (int i = 1; i <= 50; i++) {
            grafo.agregarParada(DatosDePrueba.codigo(i));
            grafo.agregarTramo("Centro", DatosDePrueba.codigo(i), i);
        }
        assertEquals(51, grafo.cantidadParadas());
        assertEquals(50, grafo.cantidadTramos());
        assertEquals(1, grafo.minutosDirectos("Centro", "P0001"));
        assertEquals(50, grafo.minutosDirectos("P0050", "Centro"));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: parada sin nombre o repetida")
    void paradasInvalidas() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        assertError("Debe indicar el nombre de la parada.", () -> grafo.agregarParada(null));
        assertError("Debe indicar el nombre de la parada.", () -> grafo.agregarParada(""));
        assertError("Debe indicar el nombre de la parada.", () -> grafo.agregarParada("   "));
        assertError("La parada 'A' ya existe.", () -> grafo.agregarParada("A"));
        assertEquals(1, grafo.cantidadParadas());
    }

    @Test
    @DisplayName("Inválido: tramo con una parada que no existe")
    void tramoConParadaInexistente() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        assertError("La parada 'Z' no existe.", () -> grafo.agregarTramo("Z", "A", 3));
        assertError("La parada 'Z' no existe.", () -> grafo.agregarTramo("A", "Z", 3));
        assertError("Debe indicar la parada de origen del tramo.", () -> grafo.agregarTramo(null, "A", 3));
        assertError("Debe indicar la parada de destino del tramo.", () -> grafo.agregarTramo("A", " ", 3));
        assertEquals(0, grafo.cantidadTramos());
    }

    @Test
    @DisplayName("Inválido: tramo con 0 o menos minutos")
    void tramoSinTiempoValido() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        assertError("El tiempo del tramo entre 'A' y 'B' debe ser mayor que cero.",
                () -> grafo.agregarTramo("A", "B", 0));
        assertError("El tiempo del tramo entre 'A' y 'B' debe ser mayor que cero.",
                () -> grafo.agregarTramo("A", "B", -4));
        assertEquals(0, grafo.cantidadTramos());
        assertEquals(-1, grafo.minutosDirectos("A", "B"));
    }

    @Test
    @DisplayName("Inválido: tramo de una parada consigo misma")
    void tramoConsigoMismo() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        assertError("Un tramo no puede unir la parada 'A' consigo misma.", () -> grafo.agregarTramo("A", "A", 2));
        assertEquals(0, grafo.cantidadTramos());
    }
}
