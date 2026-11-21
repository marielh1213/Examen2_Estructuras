package transporte.abordajes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.CIUDAD;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;
import transporte.arbol.ArbolNario;

@DisplayName("RF2 (req. 5) - Reporte con los datos de abordajes")
class ReporteAbordajesTest {

    // ------------------------------------------------------------ valores exactos (abordajes.csv)

    @Test
    @DisplayName("Reporte del CSV de mayor a menor abordaje: texto exacto")
    void reporteDeMayorAMenor() throws IOException {
        String esperado = """
                REPORTE DE ABORDAJES DIARIOS
                Ciudad: Ciudad
                Orden del listado: de mayor a menor abordaje; si hay empate, por código de parada de la A a la Z.

                Abordajes por zona:
                - Centro: 3810 abordajes
                - Norte: 2745 abordajes
                - Costa: 1280 abordajes

                Listado de paradas:
                1. S01 - Terminal (Centro): 1520 abordajes
                2. S03 - Universidad (Norte): 1310 abordajes
                3. S08 - Hospital (Centro): 1310 abordajes
                4. S02 - Mercado (Centro): 980 abordajes
                5. S05 - Estadio (Norte): 980 abordajes
                6. S04 - Faro (Costa): 640 abordajes
                7. S07 - Playa (Costa): 640 abordajes
                8. S06 - Parque (Norte): 455 abordajes

                Total de paradas: 8
                """;
        assertEquals(esperado, ReporteAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD));
    }

    @Test
    @DisplayName("Generar el reporte dos veces da el mismo texto (resultado determinista)")
    void esDeterminista() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertEquals(ReporteAbordajes.generar(arbol, CIUDAD), ReporteAbordajes.generar(arbol, CIUDAD));
    }

    // ------------------------------------------------------------ 1,200 paradas

    @Test
    @DisplayName("Req. 5: el reporte de 1,200 paradas incluye todas, en orden, y el total")
    void reporteDeMilDoscientas() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = i + 1;
        }
        String reporte = ReporteAbordajes.generar(DatosDePrueba.arbolConAbordajes(abordajes), CIUDAD);

        assertTrue(reporte.contains("1. P1200 - Parada 1200 (Zona A): 1200 abordajes\n"));
        assertTrue(reporte.contains("2. P1199 - Parada 1199 (Zona C): 1199 abordajes\n"));
        assertTrue(reporte.contains("1200. P0001 - Parada 1 (Zona B): 1 abordaje\n"));
        assertTrue(reporte.endsWith("\nTotal de paradas: 1200\n"));
        // Zona A = paradas con i % 3 == 0: 3, 6, ..., 1200 -> 400 * (3 + 1200) / 2 = 240600
        assertTrue(reporte.contains("- Zona A: 240600 abordajes\n"));
        // Zona B = i % 3 == 1: 1, 4, ..., 1198 -> 400 * (1 + 1198) / 2 = 239800
        assertTrue(reporte.contains("- Zona B: 239800 abordajes\n"));
        // Zona C = i % 3 == 2: 2, 5, ..., 1199 -> 400 * (2 + 1199) / 2 = 240200
        assertTrue(reporte.contains("- Zona C: 240200 abordajes\n"));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: una zona sin paradas aparece con 0 abordajes")
    void zonaVaciaEnElReporte() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        arbol.registrarZona(CIUDAD, "Sur");
        String reporte = ReporteAbordajes.generar(arbol, CIUDAD);

        assertTrue(reporte.contains("- Costa: 1280 abordajes\n- Sur: 0 abordajes\n"));
        assertTrue(reporte.endsWith("\nTotal de paradas: 8\n"));
    }

    @Test
    @DisplayName("Límite: con un solo abordaje se escribe en singular")
    void singular() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarParada(CIUDAD, "Centro", "S01", "Terminal", 1);
        String reporte = ReporteAbordajes.generar(arbol, CIUDAD);

        assertTrue(reporte.contains("- Centro: 1 abordaje\n"));
        assertTrue(reporte.contains("1. S01 - Terminal (Centro): 1 abordaje\n"));
    }

    @Test
    @DisplayName("Límite: una parada con 0 abordajes aparece al final con '0 abordajes'")
    void paradaSinAbordajes() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarParada(CIUDAD, "Centro", "S01", "Quieta", 0);
        arbol.registrarParada(CIUDAD, "Centro", "S02", "Activa", 4);
        String reporte = ReporteAbordajes.generar(arbol, CIUDAD);

        assertTrue(reporte.contains("1. S02 - Activa (Centro): 4 abordajes\n2. S01 - Quieta (Centro): 0 abordajes\n"));
    }

    @Test
    @DisplayName("Límite: ciudad sin paradas (con o sin zonas) muestra un aviso claro")
    void ciudadSinParadas() {
        String esperado = """
                REPORTE DE ABORDAJES DIARIOS
                Ciudad: Ciudad
                Orden del listado: de mayor a menor abordaje; si hay empate, por código de parada de la A a la Z.

                No hay paradas registradas en esta ciudad.
                """;
        ArbolNario sinZonas = new ArbolNario();
        sinZonas.registrarCiudad(CIUDAD);
        assertEquals(esperado, ReporteAbordajes.generar(sinZonas, CIUDAD));

        ArbolNario conZonaVacia = new ArbolNario();
        conZonaVacia.registrarCiudad(CIUDAD);
        conZonaVacia.registrarZona(CIUDAD, "Centro");
        assertEquals(esperado, ReporteAbordajes.generar(conZonaVacia, CIUDAD));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: ciudad que no existe, nula o en blanco")
    void ciudadInvalida() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertError("La ciudad 'Nada' no está registrada.", () -> ReporteAbordajes.generar(arbol, "Nada"));
        assertError("Debe indicar la ciudad.", () -> ReporteAbordajes.generar(arbol, null));
        assertError("Debe indicar la ciudad.", () -> ReporteAbordajes.generar(arbol, ""));
    }

    @Test
    @DisplayName("Inválido: árbol nulo")
    void argumentosNulos() {
        assertError("Debe indicar el árbol de paradas.", () -> ReporteAbordajes.generar(null, CIUDAD));
    }
}
