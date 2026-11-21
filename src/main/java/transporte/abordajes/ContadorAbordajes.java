package transporte.abordajes;

import transporte.arbol.ArbolNario;
import transporte.modelo.Parada;
import transporte.ordenamiento.RadixSort;

/**
 * Contabiliza los abordajes diarios de cualquier zona de la ciudad.
 * Los abordajes de las paradas de la zona se ordenan con Radix sort
 * (counting sort por dígito) y luego se acumulan.
 */
public final class ContadorAbordajes {

    private ContadorAbordajes() {
    }

    /**
     * Abordajes de cada parada de la zona, ordenados de menor a mayor con Radix + Counting.
     * Una zona sin paradas devuelve un arreglo vacío.
     */
    public static int[] abordajesOrdenadosDeZona(ArbolNario arbol, String ciudad, String zona) {
        if (arbol == null) {
            throw new IllegalArgumentException("Debe indicar el árbol de paradas.");
        }
        Parada[] paradas = arbol.paradasDeZona(ciudad, zona);
        int[] abordajes = new int[paradas.length];
        for (int i = 0; i < paradas.length; i++) {
            abordajes[i] = paradas[i].getAbordajes();
        }
        return RadixSort.ordenar(abordajes);
    }

    /**
     * Total de abordajes diarios de la zona. Una zona sin paradas devuelve 0.
     */
    public static long totalDeZona(ArbolNario arbol, String ciudad, String zona) {
        int[] ordenados = abordajesOrdenadosDeZona(arbol, ciudad, zona);
        long total = 0;
        for (int i = 0; i < ordenados.length; i++) {
            total += ordenados[i];
        }
        return total;
    }
}
