package transporte.ordenamiento;

/**
 * Radix sort LSD (base 10) que usa {@link CountingSort} como ordenamiento
 * estable en cada dígito. Ordena de menor a mayor.
 */
public final class RadixSort {

    private RadixSort() {
    }

    /**
     * Devuelve un arreglo nuevo con los valores ordenados de menor a mayor.
     * El arreglo recibido no se modifica.
     *
     * @throws IllegalArgumentException si el arreglo es nulo o tiene valores negativos
     */
    public static int[] ordenar(int[] datos) {
        if (datos == null) {
            throw new IllegalArgumentException("Debe indicar los datos a ordenar.");
        }
        int maximo = 0;
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] < 0) {
                throw new IllegalArgumentException(
                        "Los abordajes no pueden ser negativos (posición " + i + ").");
            }
            if (datos[i] > maximo) {
                maximo = datos[i];
            }
        }
        int[] resultado = new int[datos.length];
        System.arraycopy(datos, 0, resultado, 0, datos.length);
        for (long exp = 1; maximo / exp > 0; exp *= 10) {
            resultado = CountingSort.ordenarPorDigito(resultado, exp);
        }
        return resultado;
    }
}
