package transporte.ordenamiento;

/**
 * Counting sort estable por un dígito decimal. Es el ordenamiento interno
 * que usa {@link RadixSort} en cada pasada.
 */
public final class CountingSort {

    private CountingSort() {
    }

    /**
     * Ordena de forma estable por el dígito que corresponde a {@code exp}
     * (1 = unidades, 10 = decenas, 100 = centenas, ...) y devuelve un
     * arreglo nuevo. El arreglo recibido no se modifica.
     */
    public static int[] ordenarPorDigito(int[] datos, long exp) {
        if (datos == null) {
            throw new IllegalArgumentException("Debe indicar los datos a ordenar.");
        }
        if (exp < 1) {
            throw new IllegalArgumentException("El dígito a ordenar debe ser 1, 10, 100, ...");
        }
        for (int i = 0; i < datos.length; i++) {
            if (datos[i] < 0) {
                throw new IllegalArgumentException(
                        "Los valores no pueden ser negativos (posición " + i + ").");
            }
        }

        int[] conteo = new int[10];
        for (int i = 0; i < datos.length; i++) {
            conteo[digito(datos[i], exp)]++;
        }
        for (int d = 1; d < 10; d++) {
            conteo[d] += conteo[d - 1];
        }
        int[] salida = new int[datos.length];
        for (int i = datos.length - 1; i >= 0; i--) {
            int d = digito(datos[i], exp);
            conteo[d]--;
            salida[conteo[d]] = datos[i];
        }
        return salida;
    }

    private static int digito(int valor, long exp) {
        return (int) ((valor / exp) % 10);
    }
}
