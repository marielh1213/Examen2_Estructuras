package transporte.arbol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.assertError;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;
import transporte.modelo.Parada;

@DisplayName("RF1 (req. 1) - Registrar ciudades, zonas y paradas mediante un árbol N-ario")
class ArbolNarioTest {

    private static final String CIUDAD = "Ciudad";

    /** Ciudad con dos zonas: Centro (S01, S02) y Norte (S03). */
    private ArbolNario arbolBase() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarZona(CIUDAD, "Norte");
        arbol.registrarParada(CIUDAD, "Centro", "S01", "Terminal", 1520);
        arbol.registrarParada(CIUDAD, "Centro", "S02", "Mercado", 980);
        arbol.registrarParada(CIUDAD, "Norte", "S03", "Universidad", 1310);
        return arbol;
    }

    // ------------------------------------------------------------ valores exactos

    @Test
    @DisplayName("Registra ciudades, zonas y paradas y devuelve los valores exactos")
    void registraYDevuelveValoresExactos() {
        ArbolNario arbol = arbolBase();

        assertEquals(1, arbol.cantidadCiudades());
        assertEquals(2, arbol.cantidadZonas(CIUDAD));
        assertEquals(2, arbol.cantidadParadas(CIUDAD, "Centro"));
        assertEquals(1, arbol.cantidadParadas(CIUDAD, "Norte"));
        assertEquals(3, arbol.cantidadParadas(CIUDAD));
        assertEquals(3, arbol.cantidadParadas());
        assertArrayEquals(new String[] {"Ciudad"}, arbol.ciudades());
        assertArrayEquals(new String[] {"Centro", "Norte"}, arbol.zonas(CIUDAD));

        Parada parada = arbol.obtenerParada("S03");
        assertEquals("S03", parada.getCodigo());
        assertEquals("Universidad", parada.getNombre());
        assertEquals("Norte", parada.getZona());
        assertEquals(1310, parada.getAbordajes());
    }

    @Test
    @DisplayName("La estructura es un árbol: raíz → ciudad → zona → parada")
    void laEstructuraTieneLosNivelesEsperados() {
        Nodo raiz = arbolBase().getRaiz();
        assertEquals(Nodo.Tipo.RAIZ, raiz.getTipo());
        assertEquals(1, raiz.getCantidadHijos());

        Nodo ciudad = raiz.getHijo(0);
        assertEquals(Nodo.Tipo.CIUDAD, ciudad.getTipo());
        assertEquals("Ciudad", ciudad.getNombre());
        assertEquals(2, ciudad.getCantidadHijos());
        assertNull(ciudad.getParada());

        Nodo centro = ciudad.getHijo(0);
        assertEquals(Nodo.Tipo.ZONA, centro.getTipo());
        assertEquals("Centro", centro.getNombre());
        assertEquals(2, centro.getCantidadHijos());

        Nodo terminal = centro.getHijo(0);
        assertEquals(Nodo.Tipo.PARADA, terminal.getTipo());
        assertEquals("S01", terminal.getNombre());
        assertEquals(0, terminal.getCantidadHijos());
        assertEquals("Terminal", terminal.getParada().getNombre());
    }

    @Test
    @DisplayName("Conserva el orden en que se registraron las zonas y paradas")
    void conservaElOrdenDeRegistro() {
        ArbolNario arbol = arbolBase();
        assertArrayEquals(new String[] {"S01", "S02"}, DatosDePrueba.codigos(arbol.paradasDeZona(CIUDAD, "Centro")));
        assertArrayEquals(new String[] {"S01", "S02", "S03"}, DatosDePrueba.codigos(arbol.paradasDeCiudad(CIUDAD)));
    }

    @Test
    @DisplayName("Varias ciudades mantienen sus zonas y paradas separadas")
    void variasCiudades() {
        ArbolNario arbol = arbolBase();
        arbol.registrarCiudad("Otra");
        arbol.registrarZona("Otra", "Centro"); // el mismo nombre de zona en otra ciudad es válido
        arbol.registrarParada("Otra", "Centro", "X01", "Plaza", 10);

        assertArrayEquals(new String[] {"Ciudad", "Otra"}, arbol.ciudades());
        assertEquals(3, arbol.cantidadParadas(CIUDAD));
        assertEquals(1, arbol.cantidadParadas("Otra"));
        assertEquals(4, arbol.cantidadParadas());
        assertEquals("Centro", arbol.obtenerParada("X01").getZona());
        assertTrue(arbol.existeZona("Otra", "Centro"));
        assertFalse(arbol.existeZona("Otra", "Norte"));
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: árbol sin ciudades")
    void arbolVacio() {
        ArbolNario arbol = new ArbolNario();
        assertEquals(0, arbol.cantidadCiudades());
        assertEquals(0, arbol.ciudades().length);
        assertEquals(0, arbol.cantidadParadas());
        assertEquals(0, arbol.getRaiz().getCantidadHijos());
    }

    @Test
    @DisplayName("Límite: ciudad sin zonas")
    void ciudadSinZonas() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        assertEquals(0, arbol.cantidadZonas(CIUDAD));
        assertEquals(0, arbol.zonas(CIUDAD).length);
        assertEquals(0, arbol.cantidadParadas(CIUDAD));
        assertEquals(0, arbol.paradasDeCiudad(CIUDAD).length);
    }

    @Test
    @DisplayName("Límite: zona sin paradas")
    void zonaSinParadas() {
        ArbolNario arbol = arbolBase();
        arbol.registrarZona(CIUDAD, "Costa");
        assertEquals(3, arbol.cantidadZonas(CIUDAD));
        assertEquals(0, arbol.cantidadParadas(CIUDAD, "Costa"));
        assertEquals(0, arbol.paradasDeZona(CIUDAD, "Costa").length);
        assertEquals(3, arbol.cantidadParadas(CIUDAD));
    }

    @Test
    @DisplayName("Límite: 1,200 paradas en una misma zona (el nodo crece sin perder hijos)")
    void milDoscientasParadasEnUnaZona() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        for (int i = 1; i <= 1200; i++) {
            arbol.registrarParada(CIUDAD, "Centro", DatosDePrueba.codigo(i), "Parada " + i, i);
        }

        assertEquals(1200, arbol.cantidadParadas());
        Nodo zona = arbol.getRaiz().getHijo(0).getHijo(0);
        assertEquals(1200, zona.getCantidadHijos());
        assertEquals("P0001", zona.getHijo(0).getParada().getCodigo());
        assertEquals("P0600", zona.getHijo(599).getParada().getCodigo());
        assertEquals("P1200", zona.getHijo(1199).getParada().getCodigo());
        assertEquals(1200, arbol.obtenerParada("P1200").getAbordajes());
    }

    @Test
    @DisplayName("Límite: una parada con 0 abordajes y otra con el máximo entero son válidas")
    void abordajesEnLosExtremos() {
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        arbol.registrarZona(CIUDAD, "Centro");
        arbol.registrarParada(CIUDAD, "Centro", "A", "Cero", 0);
        arbol.registrarParada(CIUDAD, "Centro", "B", "Maxima", Integer.MAX_VALUE);
        assertEquals(0, arbol.obtenerParada("A").getAbordajes());
        assertEquals(2147483647, arbol.obtenerParada("B").getAbordajes());
    }

    @Test
    @DisplayName("Límite: pedir un hijo fuera de rango indica el error")
    void hijoFueraDeRango() {
        Nodo zona = arbolBase().getRaiz().getHijo(0).getHijo(0);
        assertError("La posición 2 no existe entre los 2 hijos del nodo.", () -> zona.getHijo(2));
        assertError("La posición -1 no existe entre los 2 hijos del nodo.", () -> zona.getHijo(-1));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: ciudad nula, vacía o en blanco")
    void ciudadSinNombre() {
        ArbolNario arbol = new ArbolNario();
        assertError("Debe indicar la ciudad.", () -> arbol.registrarCiudad(null));
        assertError("Debe indicar la ciudad.", () -> arbol.registrarCiudad(""));
        assertError("Debe indicar la ciudad.", () -> arbol.registrarCiudad("   "));
        assertEquals(0, arbol.cantidadCiudades());
    }

    @Test
    @DisplayName("Inválido: ciudad repetida")
    void ciudadRepetida() {
        ArbolNario arbol = arbolBase();
        assertError("La ciudad 'Ciudad' ya está registrada.", () -> arbol.registrarCiudad(CIUDAD));
        assertEquals(1, arbol.cantidadCiudades());
    }

    @Test
    @DisplayName("Inválido: zona en una ciudad que no existe, zona sin nombre o repetida")
    void zonasInvalidas() {
        ArbolNario arbol = arbolBase();
        assertError("La ciudad 'Nada' no está registrada.", () -> arbol.registrarZona("Nada", "Centro"));
        assertError("Debe indicar la ciudad.", () -> arbol.registrarZona(null, "Centro"));
        assertError("Debe indicar la zona.", () -> arbol.registrarZona(CIUDAD, null));
        assertError("Debe indicar la zona.", () -> arbol.registrarZona(CIUDAD, " "));
        assertError("La zona 'Centro' ya está registrada en la ciudad 'Ciudad'.",
                () -> arbol.registrarZona(CIUDAD, "Centro"));
        assertEquals(2, arbol.cantidadZonas(CIUDAD));
    }

    @Test
    @DisplayName("Inválido: parada en una ciudad o zona que no existe")
    void paradaEnUbicacionInexistente() {
        ArbolNario arbol = arbolBase();
        assertError("La ciudad 'Nada' no está registrada.",
                () -> arbol.registrarParada("Nada", "Centro", "S09", "Nueva", 1));
        assertError("La zona 'Sur' no está registrada en la ciudad 'Ciudad'.",
                () -> arbol.registrarParada(CIUDAD, "Sur", "S09", "Nueva", 1));
        assertError("Debe indicar la zona.", () -> arbol.registrarParada(CIUDAD, null, "S09", "Nueva", 1));
        assertFalse(arbol.existeParada("S09"));
        assertEquals(3, arbol.cantidadParadas());
    }

    @Test
    @DisplayName("Inválido: parada sin código o sin nombre")
    void paradaSinDatos() {
        ArbolNario arbol = arbolBase();
        assertError("Debe indicar el código de la parada.",
                () -> arbol.registrarParada(CIUDAD, "Centro", null, "Nueva", 1));
        assertError("Debe indicar el código de la parada.",
                () -> arbol.registrarParada(CIUDAD, "Centro", "", "Nueva", 1));
        assertError("Debe indicar el nombre de la parada.",
                () -> arbol.registrarParada(CIUDAD, "Centro", "S09", null, 1));
        assertError("Debe indicar el nombre de la parada.",
                () -> arbol.registrarParada(CIUDAD, "Centro", "S09", "  ", 1));
        assertEquals(3, arbol.cantidadParadas());
    }

    @Test
    @DisplayName("Inválido: abordajes negativos (la parada no se registra)")
    void abordajesNegativos() {
        ArbolNario arbol = arbolBase();
        assertError("Los abordajes de la parada S09 no pueden ser negativos.",
                () -> arbol.registrarParada(CIUDAD, "Centro", "S09", "Nueva", -1));
        assertFalse(arbol.existeParada("S09"));
        assertEquals(2, arbol.cantidadParadas(CIUDAD, "Centro"));
    }

    @Test
    @DisplayName("Inválido: código de parada repetido, en la misma zona o en otra")
    void codigoRepetido() {
        ArbolNario arbol = arbolBase();
        assertError("Ya existe una parada con el código 'S01'.",
                () -> arbol.registrarParada(CIUDAD, "Centro", "S01", "Otra", 5));
        assertError("Ya existe una parada con el código 'S01'.",
                () -> arbol.registrarParada(CIUDAD, "Norte", "S01", "Otra", 5));
        assertEquals(3, arbol.cantidadParadas());
        assertEquals("Terminal", arbol.obtenerParada("S01").getNombre());
    }

    @Test
    @DisplayName("Inválido: consultar una parada, ciudad o zona que no existe")
    void consultasInexistentes() {
        ArbolNario arbol = arbolBase();
        assertError("La parada con código 'ZZ' no existe.", () -> arbol.obtenerParada("ZZ"));
        assertFalse(arbol.existeParada("ZZ"));
        assertFalse(arbol.existeParada(null));
        assertFalse(arbol.existeCiudad(null));
        assertFalse(arbol.existeCiudad("ciudad")); // se distingue entre mayúsculas y minúsculas
        assertFalse(arbol.existeZona("Ciudad", null));
        assertFalse(arbol.existeZona("Nada", "Centro"));
        assertError("La ciudad 'Nada' no está registrada.", () -> arbol.zonas("Nada"));
        assertError("La ciudad 'Nada' no está registrada.", () -> arbol.cantidadParadas("Nada"));
        assertError("La zona 'Sur' no está registrada en la ciudad 'Ciudad'.",
                () -> arbol.paradasDeZona(CIUDAD, "Sur"));
    }
}
