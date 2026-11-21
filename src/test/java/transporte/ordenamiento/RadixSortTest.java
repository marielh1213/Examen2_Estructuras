package transporte.ordenamiento;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.assertError;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RF1 (req. 2) - Ordenamiento Radix con Counting")
class RadixSortTest {

    // ------------------------------------------------------------ valores exactos

    @Test
    @DisplayName("Ordena los abordajes del CSV de menor a mayor")
    void ordenaLosAbordajesDelCsv() {
        int[] abordajes = {1520, 980, 1310, 640, 980, 455, 640, 1310};
        assertArrayEquals(new int[] {455, 640, 640, 980, 980, 1310, 1310, 1520}, RadixSort.ordenar(abordajes));
    }

    @Test
    @DisplayName("Ordena números con distinta cantidad de dígitos")
    void distintaCantidadDeDigitos() {
        int[] datos = {100, 9, 10, 0, 99, 1000, 1, 11};
        assertArrayEquals(new int[] {0, 1, 9, 10, 11, 99, 100, 1000}, RadixSort.ordenar(datos));
    }

    @Test
    @DisplayName("No modifica el arreglo recibido y devuelve uno nuevo")
    void noModificaElOriginal() {
        int[] datos = {3, 1, 2};
        int[] ordenado = RadixSort.ordenar(datos);
        assertArrayEquals(new int[] {3, 1, 2}, datos);
        assertArrayEquals(new int[] {1, 2, 3}, ordenado);
        assertNotSame(datos, ordenado);
    }

    @Test
    @DisplayName("1,200 valores: queda en orden y es una permutación de la entrada")
    void milDoscientosValores() {
        int[] datos = new int[1200];
        int semilla = 12345;
        for (int i = 0; i < datos.length; i++) {
            semilla = (semilla * 1103515245 + 12345) & 0x7fffffff;
            datos[i] = semilla % 2000;
        }
        int[] ordenado = RadixSort.ordenar(datos);

        assertEquals(1200, ordenado.length);
        int[] histogramaEntrada = new int[2000];
        int[] histogramaSalida = new int[2000];
        for (int i = 0; i < datos.length; i++) {
            histogramaEntrada[datos[i]]++;
            histogramaSalida[ordenado[i]]++;
            if (i > 0) {
                assertTrue(ordenado[i - 1] <= ordenado[i], "fuera de orden en la posición " + i);
            }
        }
        assertArrayEquals(histogramaEntrada, histogramaSalida);
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: arreglo vacío")
    void vacio() {
        assertEquals(0, RadixSort.ordenar(new int[0]).length);
    }

    @Test
    @DisplayName("Límite: un solo elemento")
    void unElemento() {
        assertArrayEquals(new int[] {42}, RadixSort.ordenar(new int[] {42}));
    }

    @Test
    @DisplayName("Límite: todos los valores iguales")
    void todosIguales() {
        assertArrayEquals(new int[] {7, 7, 7, 7}, RadixSort.ordenar(new int[] {7, 7, 7, 7}));
    }

    @Test
    @DisplayName("Límite: todos los valores son cero")
    void todosCero() {
        assertArrayEquals(new int[] {0, 0, 0}, RadixSort.ordenar(new int[] {0, 0, 0}));
    }

    @Test
    @DisplayName("Límite: ya ordenado y en orden inverso")
    void yaOrdenadoEInverso() {
        assertArrayEquals(new int[] {1, 2, 3, 4, 5}, RadixSort.ordenar(new int[] {1, 2, 3, 4, 5}));
        assertArrayEquals(new int[] {1, 2, 3, 4, 5}, RadixSort.ordenar(new int[] {5, 4, 3, 2, 1}));
    }

    @Test
    @DisplayName("Límite: el valor entero más grande (10 dígitos)")
    void valorMaximo() {
        assertArrayEquals(new int[] {0, 2147483646, 2147483647},
                RadixSort.ordenar(new int[] {Integer.MAX_VALUE, 0, 2147483646}));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: arreglo nulo")
    void arregloNulo() {
        assertError("Debe indicar los datos a ordenar.", () -> RadixSort.ordenar(null));
    }

    @Test
    @DisplayName("Inválido: valores negativos")
    void valoresNegativos() {
        assertError("Los abordajes no pueden ser negativos (posición 1).",
                () -> RadixSort.ordenar(new int[] {5, -1, 3}));
        assertError("Los abordajes no pueden ser negativos (posición 0).",
                () -> RadixSort.ordenar(new int[] {Integer.MIN_VALUE}));
    }
}
