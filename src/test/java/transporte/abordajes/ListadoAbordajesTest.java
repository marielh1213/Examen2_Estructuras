package transporte.abordajes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.CIUDAD;
import static transporte.DatosDePrueba.assertError;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;
import transporte.arbol.ArbolNario;
import transporte.modelo.Parada;

@DisplayName("RF2 (req. 3 y 4) - Listado de paradas de mayor a menor abordaje, empates por código")
class ListadoAbordajesTest {

    // ------------------------------------------------------------ valores exactos (abordajes.csv)

    @Test
    @DisplayName("Listado del CSV: códigos, nombres y abordajes exactos")
    void listadoDelCsv() throws IOException {
        Parada[] listado = ListadoAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD);

        assertArrayEquals(new String[] {"S01", "S03", "S08", "S02", "S05", "S04", "S07", "S06"},
                DatosDePrueba.codigos(listado));
        assertArrayEquals(new int[] {1520, 1310, 1310, 980, 980, 640, 640, 455}, DatosDePrueba.abordajes(listado));
        assertEquals("Terminal", listado[0].getNombre());
        assertEquals("Universidad", listado[1].getNombre());
        assertEquals("Hospital", listado[2].getNombre());
        assertEquals("Parque", listado[7].getNombre());
    }

    @Test
    @DisplayName("Los empates entre zonas distintas también se resuelven por código (S03 Norte antes que S08 Centro)")
    void empateEntreZonas() throws IOException {
        Parada[] listado = ListadoAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD);
        assertEquals("Norte", listado[1].getZona());
        assertEquals("Centro", listado[2].getZona());
        assertEquals(listado[1].getAbordajes(), listado[2].getAbordajes());
    }

    @Test
    @DisplayName("Generar el listado no modifica el árbol y devuelve un arreglo nuevo cada vez")
    void noModificaElArbol() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        String[] antes = DatosDePrueba.codigos(arbol.paradasDeCiudad(CIUDAD));
        Parada[] primero = ListadoAbordajes.generar(arbol, CIUDAD);
        Parada[] segundo = ListadoAbordajes.generar(arbol, CIUDAD);

        assertArrayEquals(antes, DatosDePrueba.codigos(arbol.paradasDeCiudad(CIUDAD)));
        assertNotSame(primero, segundo);
        assertArrayEquals(DatosDePrueba.codigos(primero), DatosDePrueba.codigos(segundo));
    }

    // ------------------------------------------------------------ 1,200 paradas

    @Test
    @DisplayName("Req. 3: listado de 1,200 paradas repartidas en tres zonas")
    void listadoDeMilDoscientas() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = (i + 1) % 100;
        }
        ArbolNario arbol = DatosDePrueba.arbolConAbordajes(abordajes);
        Parada[] listado = ListadoAbordajes.generar(arbol, CIUDAD);

        assertEquals(1200, listado.length);
        assertEquals("P0099", listado[0].getCodigo()); // 99 abordajes, el código más bajo
        assertEquals(99, listado[0].getAbordajes());
        assertEquals("P1200", listado[1199].getCodigo()); // 0 abordajes, el código más alto
        assertEquals(0, listado[1199].getAbordajes());
        for (int i = 1; i < listado.length; i++) {
            Parada anterior = listado[i - 1];
            Parada actual = listado[i];
            assertTrue(anterior.getAbordajes() >= actual.getAbordajes(), "abordajes fuera de orden en " + i);
            if (anterior.getAbordajes() == actual.getAbordajes()) {
                assertTrue(anterior.getCodigo().compareTo(actual.getCodigo()) < 0,
                        "empate fuera de orden por código en " + i);
            }
        }
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: ciudad sin paradas devuelve un listado vacío")
    void ciudadSinParadas() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        assertEquals(0, ListadoAbordajes.generar(arbol, CIUDAD).length);
    }

    @Test
    @DisplayName("Límite: ciudad con una sola parada")
    void unaSolaParada() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarParada(CIUDAD, "Centro", "S01", "Terminal", 10);
        Parada[] listado = ListadoAbordajes.generar(arbol, CIUDAD);
        assertArrayEquals(new String[] {"S01"}, DatosDePrueba.codigos(listado));
    }

    @Test
    @DisplayName("Límite: todas las paradas empatadas quedan por código de A a Z")
    void todasEmpatadas() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarParada(CIUDAD, "Centro", "C3", "c", 10);
        arbol.registrarParada(CIUDAD, "Centro", "A1", "a", 10);
        arbol.registrarParada(CIUDAD, "Centro", "B2", "b", 10);
        assertArrayEquals(new String[] {"A1", "B2", "C3"},
                DatosDePrueba.codigos(ListadoAbordajes.generar(arbol, CIUDAD)));
    }

    @Test
    @DisplayName("Límite: solo se listan las paradas de la ciudad pedida")
    void soloLaCiudadPedida() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        arbol.registrarCiudad("Otra");
        arbol.registrarZona("Otra", "Centro");
        arbol.registrarParada("Otra", "Centro", "X01", "Plaza", 99999);

        assertEquals(8, ListadoAbordajes.generar(arbol, CIUDAD).length);
        assertArrayEquals(new String[] {"X01"}, DatosDePrueba.codigos(ListadoAbordajes.generar(arbol, "Otra")));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: ciudad que no existe, nula o en blanco")
    void ciudadInvalida() throws IOException {
        ArbolNario arbol = DatosDePrueba.arbolDesdeCsv();
        assertError("La ciudad 'Nada' no está registrada.", () -> ListadoAbordajes.generar(arbol, "Nada"));
        assertError("Debe indicar la ciudad.", () -> ListadoAbordajes.generar(arbol, null));
        assertError("Debe indicar la ciudad.", () -> ListadoAbordajes.generar(arbol, " "));
    }

    @Test
    @DisplayName("Inválido: árbol nulo")
    void argumentosNulos() {
        assertError("Debe indicar el árbol de paradas.", () -> ListadoAbordajes.generar(null, CIUDAD));
    }
}
