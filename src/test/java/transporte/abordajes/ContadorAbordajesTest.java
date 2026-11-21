package transporte.abordajes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static transporte.DatosDePrueba.CIUDAD;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;
import transporte.arbol.ArbolNario;

@DisplayName("RF1 (req. 2) - Contabilizar los abordajes diarios de cualquier zona")
class ContadorAbordajesTest {

    // ------------------------------------------------------------ valores exactos (abordajes.csv)

    @Test
    @DisplayName("Total de cada zona del CSV: Centro 3810, Norte 2745, Costa 1280")
    void totalesPorZona() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertEquals(3810L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Centro")); // 1520 + 980 + 1310
        assertEquals(2745L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Norte"));  // 1310 + 980 + 455
        assertEquals(1280L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Costa"));  // 640 + 640
    }

    @Test
    @DisplayName("La suma de las tres zonas es el total de la ciudad: 7835")
    void sumaDeZonasIgualAlTotal() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        long suma = 0;
        String[] zonas = arbol.zonas(CIUDAD);
        for (int i = 0; i < zonas.length; i++) {
            suma += ContadorAbordajes.totalDeZona(arbol, CIUDAD, zonas[i]);
        }
        assertEquals(7835L, suma);
    }

    @Test
    @DisplayName("Abordajes de cada zona ordenados con Radix + Counting")
    void abordajesOrdenadosPorZona() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertArrayEquals(new int[] {980, 1310, 1520},
                ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Centro"));
        assertArrayEquals(new int[] {455, 980, 1310},
                ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Norte"));
        assertArrayEquals(new int[] {640, 640},
                ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Costa"));
    }

    @Test
    @DisplayName("Los abordajes de la zona se cuentan solo en su zona, aunque otra ciudad use el mismo nombre")
    void zonaConMismoNombreEnOtraCiudad() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        arbol.registrarCiudad("Otra");
        arbol.registrarZona("Otra", "Centro");
        arbol.registrarParada("Otra", "Centro", "X01", "Plaza", 5);

        assertEquals(3810L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Centro"));
        assertEquals(5L, ContadorAbordajes.totalDeZona(arbol, "Otra", "Centro"));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: una zona vacía tiene 0 abordajes y ningún valor")
    void zonaVacia() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        arbol.registrarZona(CIUDAD, "Sur");
        assertEquals(0L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Sur"));
        assertEquals(0, ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Sur").length);
    }

    @Test
    @DisplayName("Límite: una zona con una sola parada")
    void zonaConUnaParada() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Unica");
        arbol.registrarParada(CIUDAD, "Unica", "U1", "Sola", 77);
        assertEquals(77L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Unica"));
        assertArrayEquals(new int[] {77}, ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Unica"));
    }

    @Test
    @DisplayName("Límite: paradas con 0 abordajes cuentan como 0 y no se pierden")
    void paradasConCeroAbordajes() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Quieta");
        arbol.registrarParada(CIUDAD, "Quieta", "Q1", "Uno", 0);
        arbol.registrarParada(CIUDAD, "Quieta", "Q2", "Dos", 5);
        arbol.registrarParada(CIUDAD, "Quieta", "Q3", "Tres", 0);
        assertEquals(5L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Quieta"));
        assertArrayEquals(new int[] {0, 0, 5}, ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Quieta"));
    }

    @Test
    @DisplayName("Límite: el total puede superar el máximo de un entero (3 x 2147483647 = 6442450941)")
    void totalMayorQueUnEntero() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Enorme");
        arbol.registrarParada(CIUDAD, "Enorme", "E1", "Uno", Integer.MAX_VALUE);
        arbol.registrarParada(CIUDAD, "Enorme", "E2", "Dos", Integer.MAX_VALUE);
        arbol.registrarParada(CIUDAD, "Enorme", "E3", "Tres", Integer.MAX_VALUE);
        assertEquals(6442450941L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Enorme"));
    }

    @Test
    @DisplayName("Límite: una zona con 400 paradas de 1,200 en la ciudad")
    void zonaGrande() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = i + 1; // la parada i tiene i abordajes
        }
        ArbolNario arbol = DatosDePrueba.arbolConAbordajes(abordajes);
        // La zona C recibe las paradas con i % 3 == 2: 2, 5, 8, ..., 1199 -> 400 paradas.
        // Suma = 400 * (2 + 1199) / 2 = 240200
        assertEquals(400, arbol.cantidadParadas(CIUDAD, "Zona C"));
        assertEquals(240200L, ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Zona C"));
        int[] ordenados = ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Zona C");
        assertEquals(2, ordenados[0]);
        assertEquals(1199, ordenados[399]);
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: zona que no existe (también si cambia una mayúscula)")
    void zonaInexistente() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertError("La zona 'Sur' no está registrada en la ciudad 'Ciudad'.",
                () -> ContadorAbordajes.totalDeZona(arbol, CIUDAD, "Sur"));
        assertError("La zona 'centro' no está registrada en la ciudad 'Ciudad'.",
                () -> ContadorAbordajes.totalDeZona(arbol, CIUDAD, "centro"));
        assertError("La zona 'Sur' no está registrada en la ciudad 'Ciudad'.",
                () -> ContadorAbordajes.abordajesOrdenadosDeZona(arbol, CIUDAD, "Sur"));
    }

    @Test
    @DisplayName("Inválido: ciudad que no existe")
    void ciudadInexistente() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertError("La ciudad 'Nada' no está registrada.",
                () -> ContadorAbordajes.totalDeZona(arbol, "Nada", "Centro"));
    }

    @Test
    @DisplayName("Inválido: ciudad o zona nula o en blanco, y árbol nulo")
    void datosFaltantes() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertError("Debe indicar la ciudad.", () -> ContadorAbordajes.totalDeZona(arbol, null, "Centro"));
        assertError("Debe indicar la zona.", () -> ContadorAbordajes.totalDeZona(arbol, CIUDAD, null));
        assertError("Debe indicar la zona.", () -> ContadorAbordajes.totalDeZona(arbol, CIUDAD, "  "));
        assertError("Debe indicar el árbol de paradas.",
                () -> ContadorAbordajes.totalDeZona(null, CIUDAD, "Centro"));
    }
}
