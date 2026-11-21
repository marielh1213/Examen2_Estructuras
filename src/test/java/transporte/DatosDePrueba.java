package transporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.function.Executable;

import transporte.arbol.ArbolNario;
import transporte.datos.CargadorCsv;
import transporte.grafo.Grafo;
import transporte.modelo.Parada;

/**
 * Datos y utilidades compartidas por las pruebas. No usa colecciones de la
 * biblioteca estándar, solo arreglos.
 */
public final class DatosDePrueba {

    public static final String CIUDAD = "Ciudad";
    public static final Path ABORDAJES = Path.of("datos", "abordajes.csv");
    public static final Path TRAMOS = Path.of("datos", "tramos.csv");

    /** Nombres de las 8 paradas que aparecen en los dos CSV. */
    public static final String[] NOMBRES_PARADAS = {
        "Terminal", "Mercado", "Universidad", "Faro", "Estadio", "Parque", "Playa", "Hospital"
    };

    private DatosDePrueba() {
    }

    /** Árbol cargado con datos/abordajes.csv dentro de la ciudad "Ciudad". */
    public static ArbolNario arbolDesdeCsv() throws IOException {
        ArbolNario arbol = new ArbolNario();
        CargadorCsv.cargarAbordajes(ABORDAJES, CIUDAD, arbol);
        return arbol;
    }

    /** Grafo cargado con datos/tramos.csv. */
    public static Grafo grafoDesdeCsv() throws IOException {
        return CargadorCsv.cargarTramos(TRAMOS);
    }

    /** Código con cuatro dígitos: codigo(7) = "P0007", codigo(1200) = "P1200". */
    public static String codigo(int numero) {
        String digitos = String.valueOf(numero);
        while (digitos.length() < 4) {
            digitos = "0" + digitos;
        }
        return "P" + digitos;
    }

    /**
     * Árbol con una ciudad ("Ciudad"), tres zonas ("Zona A", "Zona B", "Zona C")
     * y una parada por cada valor recibido. La parada i (desde 1) tiene código
     * codigo(i), nombre "Parada i" y abordajes[i - 1]; va en la zona i % 3.
     */
    public static ArbolNario arbolConAbordajes(int[] abordajes) {
        String[] zonas = {"Zona A", "Zona B", "Zona C"};
        ArbolNario arbol = new ArbolNario();
        arbol.registrarCiudad(CIUDAD);
        for (int z = 0; z < zonas.length; z++) {
            arbol.registrarZona(CIUDAD, zonas[z]);
        }
        for (int i = 1; i <= abordajes.length; i++) {
            arbol.registrarParada(CIUDAD, zonas[i % 3], codigo(i), "Parada " + i, abordajes[i - 1]);
        }
        return arbol;
    }

    /** Arreglo de paradas con códigos codigo(1..n) y los abordajes indicados. */
    public static Parada[] paradasConAbordajes(int[] abordajes) {
        Parada[] paradas = new Parada[abordajes.length];
        for (int i = 0; i < abordajes.length; i++) {
            paradas[i] = new Parada(codigo(i + 1), "Parada " + (i + 1), "Zona A", abordajes[i]);
        }
        return paradas;
    }

    public static String[] codigos(Parada[] paradas) {
        String[] codigos = new String[paradas.length];
        for (int i = 0; i < paradas.length; i++) {
            codigos[i] = paradas[i].getCodigo();
        }
        return codigos;
    }

    public static int[] abordajes(Parada[] paradas) {
        int[] valores = new int[paradas.length];
        for (int i = 0; i < paradas.length; i++) {
            valores[i] = paradas[i].getAbordajes();
        }
        return valores;
    }

    /** Verifica que la acción lance IllegalArgumentException y devuelve su mensaje. */
    public static String errorDe(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    /** Verifica que la acción lance IllegalArgumentException con exactamente ese mensaje. */
    public static void assertError(String mensajeEsperado, Executable accion) {
        assertEquals(mensajeEsperado, errorDe(accion));
    }
}
