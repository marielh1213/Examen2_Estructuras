package transporte.ordenamiento;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static transporte.DatosDePrueba.assertError;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RF1 (req. 2) - Counting sort por dígito (paso interno de Radix)")
class CountingSortTest {

    @Test
    @DisplayName("Ordena por el dígito de las unidades de forma estable")
    void porUnidades() {
        // dígitos de las unidades: 2, 1, 2, 1 -> 21, 11 (en su orden original), luego 32, 12
        assertArrayEquals(new int[] {21, 11, 32, 12},
                CountingSort.ordenarPorDigito(new int[] {32, 21, 12, 11}, 1));
    }

    @Test
    @DisplayName("Ordena por el dígito de las decenas de forma estable")
    void porDecenas() {
        // dígitos de las decenas: 3, 2, 1, 1 -> 12, 11 (en su orden original), 21, 32
        assertArrayEquals(new int[] {12, 11, 21, 32},
                CountingSort.ordenarPorDigito(new int[] {32, 21, 12, 11}, 10));
    }

    @Test
    @DisplayName("Una potencia de 10 mayor que todos los valores no cambia el orden")
    void digitoQueNoExiste() {
        assertArrayEquals(new int[] {5, 3, 9}, CountingSort.ordenarPorDigito(new int[] {5, 3, 9}, 100));
    }

    @Test
    @DisplayName("Límite: arreglo vacío y un solo elemento")
    void limites() {
        assertEquals(0, CountingSort.ordenarPorDigito(new int[0], 1).length);
        assertArrayEquals(new int[] {8}, CountingSort.ordenarPorDigito(new int[] {8}, 1));
    }

    @Test
    @DisplayName("No modifica el arreglo recibido")
    void noModificaElOriginal() {
        int[] datos = {32, 21, 12, 11};
        CountingSort.ordenarPorDigito(datos, 1);
        assertArrayEquals(new int[] {32, 21, 12, 11}, datos);
    }

    @Test
    @DisplayName("Inválido: arreglo nulo, dígito incorrecto o valor negativo")
    void invalidos() {
        assertError("Debe indicar los datos a ordenar.", () -> CountingSort.ordenarPorDigito(null, 1));
        assertError("El dígito a ordenar debe ser 1, 10, 100, ...",
                () -> CountingSort.ordenarPorDigito(new int[] {1}, 0));
        assertError("El dígito a ordenar debe ser 1, 10, 100, ...",
                () -> CountingSort.ordenarPorDigito(new int[] {1}, -10));
        assertError("Los valores no pueden ser negativos (posición 0).",
                () -> CountingSort.ordenarPorDigito(new int[] {-4, 2}, 1));
    }
}
