package transporte.grafo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;

@DisplayName("RF3 (req. 6) - Ruta más corta y tiempo estimado con Dijkstra sobre un grafo no dirigido")
class DijkstraTest {

    /** Verifica los minutos y el recorrido exacto; {@code recorrido[0]} es la salida y el último el destino. */
    private void verificar(Grafo grafo, int minutos, String... recorrido) {
        String salida = recorrido[0];
        String destino = recorrido[recorrido.length - 1];
        Ruta ruta = Dijkstra.rutaMasCorta(grafo, salida, destino);
        assertEquals(minutos, ruta.getMinutos());
        assertArrayEquals(recorrido, ruta.getParadas());
        assertEquals(salida, ruta.getOrigen());
        assertEquals(destino, ruta.getDestino());
        assertEquals(recorrido.length, ruta.getCantidadParadas());
    }

    /** Suma los minutos de los tramos directos entre paradas consecutivas de la ruta. */
    private int sumaDelRecorrido(Grafo grafo, Ruta ruta) {
        String[] paradas = ruta.getParadas();
        int suma = 0;
        for (int i = 1; i < paradas.length; i++) {
            int tramo = grafo.minutosDirectos(paradas[i - 1], paradas[i]);
            assertTrue(tramo > 0, "no hay tramo directo entre " + paradas[i - 1] + " y " + paradas[i]);
            suma += tramo;
        }
        return suma;
    }

    // ------------------------------------------------------------ valores exactos (tramos.csv)

    @Test
    @DisplayName("Terminal → Faro: 15 minutos por Estadio y Parque")
    void terminalAFaro() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 15, "Terminal", "Estadio", "Parque", "Faro");
    }

    @Test
    @DisplayName("Terminal → Hospital: 10 minutos por Mercado")
    void terminalAHospital() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 10, "Terminal", "Mercado", "Hospital");
    }

    @Test
    @DisplayName("Terminal → Playa: 12 minutos por el tramo directo (el desvío por Faro cuesta 20)")
    void terminalAPlaya() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 12, "Terminal", "Playa");
    }

    @Test
    @DisplayName("Terminal → Universidad: 11 minutos por Estadio y Parque")
    void terminalAUniversidad() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 11, "Terminal", "Estadio", "Parque", "Universidad");
    }

    @Test
    @DisplayName("Hospital → Playa: 16 minutos por Universidad, Parque y Faro (el tramo directo de Universidad cuesta 15)")
    void hospitalAPlaya() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 16, "Hospital", "Universidad", "Parque", "Faro", "Playa");
    }

    @Test
    @DisplayName("Estadio → Hospital: 9 minutos por Parque y Universidad")
    void estadioAHospital() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 9, "Estadio", "Parque", "Universidad", "Hospital");
    }

    @Test
    @DisplayName("Playa → Mercado: 18 minutos por Terminal")
    void playaAMercado() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 18, "Playa", "Terminal", "Mercado");
    }

    @Test
    @DisplayName("Universidad → Playa: 13 minutos por Parque y Faro")
    void universidadAPlaya() throws IOException {
        verificar(DatosDePrueba.grafoDesdeCsv(), 13, "Universidad", "Parque", "Faro", "Playa");
    }

    @Test
    @DisplayName("Tiempos exactos desde Terminal hacia las 8 paradas")
    void tiemposDesdeTerminal() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        String[] destinos = {"Terminal", "Estadio", "Mercado", "Parque", "Hospital", "Universidad", "Playa", "Faro"};
        int[] minutos = {0, 5, 6, 9, 10, 11, 12, 15};
        for (int i = 0; i < destinos.length; i++) {
            assertEquals(minutos[i], Dijkstra.rutaMasCorta(grafo, "Terminal", destinos[i]).getMinutos(),
                    "Terminal → " + destinos[i]);
        }
    }

    // ------------------------------------------------------------ grafo no dirigido

    @Test
    @DisplayName("No dirigido: la ruta de ida y la de vuelta tienen el mismo tiempo y recorrido inverso")
    void idaYVuelta() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        verificar(grafo, 15, "Terminal", "Estadio", "Parque", "Faro");
        verificar(grafo, 15, "Faro", "Parque", "Estadio", "Terminal");
    }

    @Test
    @DisplayName("No dirigido: un tramo de línea Parada→Parada también se recorre al revés")
    void tramosContraLaDireccionDeLaLinea() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        verificar(grafo, 2, "Universidad", "Parque"); // Bus 4 solo figura de Parque a Universidad
        verificar(grafo, 5, "Playa", "Faro");         // Bus 2 solo figura de Faro a Playa
        verificar(grafo, 12, "Terminal", "Playa");    // Bus 2 figura de Playa a Terminal
    }

    @Test
    @DisplayName("No dirigido: los 64 pares de paradas dan el mismo tiempo en ambos sentidos")
    void simetriaEnTodosLosPares() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        for (int i = 0; i < DatosDePrueba.NOMBRES_PARADAS.length; i++) {
            for (int j = 0; j < DatosDePrueba.NOMBRES_PARADAS.length; j++) {
                int ida = Dijkstra.rutaMasCorta(grafo, DatosDePrueba.NOMBRES_PARADAS[i], DatosDePrueba.NOMBRES_PARADAS[j]).getMinutos();
                int vuelta = Dijkstra.rutaMasCorta(grafo, DatosDePrueba.NOMBRES_PARADAS[j], DatosDePrueba.NOMBRES_PARADAS[i]).getMinutos();
                assertEquals(ida, vuelta, DatosDePrueba.NOMBRES_PARADAS[i] + " ↔ " + DatosDePrueba.NOMBRES_PARADAS[j]);
            }
        }
    }

    @Test
    @DisplayName("La caminata Mercado ↔ Parque (10 min) no es la ruta más corta: por Universidad son 9")
    void caminataNoEsLaMasCorta() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertEquals(10, grafo.minutosDirectos("Mercado", "Parque"));
        assertEquals(10, grafo.minutosDirectos("Parque", "Mercado"));

        Ruta ida = Dijkstra.rutaMasCorta(grafo, "Mercado", "Parque");
        Ruta vuelta = Dijkstra.rutaMasCorta(grafo, "Parque", "Mercado");
        assertEquals(9, ida.getMinutos());
        assertEquals(9, vuelta.getMinutos());
        assertEquals(9, sumaDelRecorrido(grafo, ida));
        assertEquals(9, sumaDelRecorrido(grafo, vuelta));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: empate entre rutas (Mercado → Universidad = 7 por el tramo directo o por Hospital)")
    void empateEntreRutas() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        Ruta ruta = Dijkstra.rutaMasCorta(grafo, "Mercado", "Universidad");
        assertEquals(7, ruta.getMinutos());
        assertEquals("Mercado", ruta.getOrigen());
        assertEquals("Universidad", ruta.getDestino());
        assertEquals(7, sumaDelRecorrido(grafo, ruta)); // el recorrido devuelto es uno de los dos válidos
    }

    @Test
    @DisplayName("Límite: origen igual al destino devuelve 0 minutos y solo esa parada")
    void origenIgualDestino() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        for (int i = 0; i < DatosDePrueba.NOMBRES_PARADAS.length; i++) {
            verificar(grafo, 0, DatosDePrueba.NOMBRES_PARADAS[i]);
        }
    }

    @Test
    @DisplayName("Límite: parada sin ningún tramo; hacia sí misma es 0 y hacia otra no hay ruta")
    void paradaAislada() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        grafo.agregarParada("Sola");
        grafo.agregarTramo("A", "B", 3);

        verificar(grafo, 0, "Sola");
        RutaNoEncontradaException error =
                assertThrows(RutaNoEncontradaException.class, () -> Dijkstra.rutaMasCorta(grafo, "A", "Sola"));
        assertEquals("No hay una ruta para ir de 'A' a 'Sola'.", error.getMessage());
        error = assertThrows(RutaNoEncontradaException.class, () -> Dijkstra.rutaMasCorta(grafo, "Sola", "B"));
        assertEquals("No hay una ruta para ir de 'Sola' a 'B'.", error.getMessage());
    }

    @Test
    @DisplayName("Límite: dos grupos de paradas sin conexión entre sí")
    void gruposSeparados() {
        Grafo grafo = new Grafo();
        String[] nombres = {"A", "B", "C", "D"};
        for (int i = 0; i < nombres.length; i++) {
            grafo.agregarParada(nombres[i]);
        }
        grafo.agregarTramo("A", "B", 1);
        grafo.agregarTramo("C", "D", 1);

        verificar(grafo, 1, "A", "B");
        verificar(grafo, 1, "D", "C");
        assertThrows(RutaNoEncontradaException.class, () -> Dijkstra.rutaMasCorta(grafo, "A", "D"));
        assertThrows(RutaNoEncontradaException.class, () -> Dijkstra.rutaMasCorta(grafo, "C", "B"));
    }

    @Test
    @DisplayName("Límite: un tramo directo pesado pierde contra un rodeo más liviano")
    void rodeoMasLiviano() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        grafo.agregarParada("C");
        grafo.agregarTramo("A", "B", 10);
        grafo.agregarTramo("A", "C", 1);
        grafo.agregarTramo("C", "B", 1);
        verificar(grafo, 2, "A", "C", "B");
        verificar(grafo, 2, "B", "C", "A");
    }

    @Test
    @DisplayName("Límite: dos tramos entre las mismas paradas, gana el de menos minutos")
    void tramosParalelos() {
        Grafo grafo = new Grafo();
        grafo.agregarParada("A");
        grafo.agregarParada("B");
        grafo.agregarTramo("A", "B", 5);
        grafo.agregarTramo("A", "B", 3);
        verificar(grafo, 3, "A", "B");
        verificar(grafo, 3, "B", "A");
    }

    @Test
    @DisplayName("Límite: 1,200 paradas en cadena (el grafo crece sin perder datos)")
    void cadenaDeMilDoscientas() {
        Grafo grafo = new Grafo();
        for (int i = 1; i <= 1200; i++) {
            grafo.agregarParada(DatosDePrueba.codigo(i));
        }
        for (int i = 1; i < 1200; i++) {
            grafo.agregarTramo(DatosDePrueba.codigo(i), DatosDePrueba.codigo(i + 1), 1);
        }
        assertEquals(1200, grafo.cantidadParadas());
        assertEquals(1199, grafo.cantidadTramos());

        Ruta ida = Dijkstra.rutaMasCorta(grafo, "P0001", "P1200");
        assertEquals(1199, ida.getMinutos());
        assertEquals(1200, ida.getCantidadParadas());
        assertEquals("P0001", ida.getParadas()[0]);
        assertEquals("P0600", ida.getParadas()[599]);
        assertEquals("P1200", ida.getParadas()[1199]);

        Ruta vuelta = Dijkstra.rutaMasCorta(grafo, "P1200", "P0001");
        assertEquals(1199, vuelta.getMinutos());
        assertEquals("P0001", vuelta.getParadas()[1199]);
    }

    @Test
    @DisplayName("Límite: cambiar el arreglo devuelto por getParadas() no altera la ruta")
    void getParadasDevuelveCopia() throws IOException {
        Ruta ruta = Dijkstra.rutaMasCorta(DatosDePrueba.grafoDesdeCsv(), "Terminal", "Faro");
        ruta.getParadas()[0] = "Otra";
        assertEquals("Terminal", ruta.getParadas()[0]);
        assertEquals("Terminal", ruta.getOrigen());
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: parada de salida que no existe")
    void salidaInexistente() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertError("La parada 'Zoo' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "Zoo", "Faro"));
    }

    @Test
    @DisplayName("Inválido: parada de destino que no existe")
    void destinoInexistente() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertError("La parada 'Zoo' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "Terminal", "Zoo"));
    }

    @Test
    @DisplayName("Inválido: si ninguna de las dos existe se avisa primero de la salida")
    void ningunaExiste() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertError("La parada 'Zoo' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "Zoo", "Circo"));
    }

    @Test
    @DisplayName("Inválido: el nombre de la parada debe coincidir exactamente (mayúsculas incluidas)")
    void nombreDebeCoincidir() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertError("La parada 'terminal' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "terminal", "Faro"));
        assertError("La parada 'Faro ' no existe.", () -> Dijkstra.rutaMasCorta(grafo, "Terminal", "Faro "));
    }

    @Test
    @DisplayName("Inválido: salida o destino nulos o en blanco")
    void datosFaltantes() throws IOException {
        Grafo grafo = DatosDePrueba.grafoDesdeCsv();
        assertError("Debe indicar la parada de salida.", () -> Dijkstra.rutaMasCorta(grafo, null, "Faro"));
        assertError("Debe indicar la parada de salida.", () -> Dijkstra.rutaMasCorta(grafo, "", "Faro"));
        assertError("Debe indicar la parada de destino.", () -> Dijkstra.rutaMasCorta(grafo, "Terminal", null));
        assertError("Debe indicar la parada de destino.", () -> Dijkstra.rutaMasCorta(grafo, "Terminal", "  "));
        assertError("Debe indicar la parada de salida.", () -> Dijkstra.rutaMasCorta(grafo, null, null));
    }

    @Test
    @DisplayName("Inválido: grafo nulo")
    void grafoNulo() {
        assertError("Debe indicar el mapa de paradas.", () -> Dijkstra.rutaMasCorta(null, "A", "B"));
    }
}
