package transporte.abordajes;

import transporte.arbol.ArbolNario;
import transporte.modelo.Parada;
import transporte.ordenamiento.Criterios;
import transporte.ordenamiento.QuickSort;

/**
 * Genera el listado de paradas de una ciudad ordenado con QuickSort: de mayor a
 * menor abordaje y, en caso de empate, por código de parada de la A a la Z.
 */
public final class ListadoAbordajes {

    private ListadoAbordajes() {
    }

    /**
     * Devuelve un arreglo nuevo con todas las paradas de la ciudad ordenadas de
     * mayor a menor abordaje; en caso de empate, por código de parada de la A a la Z.
     * El árbol no se modifica.
     */
    public static Parada[] generar(ArbolNario arbol, String ciudad) {
        if (arbol == null) {
            throw new IllegalArgumentException("Debe indicar el árbol de paradas.");
        }
        Parada[] paradas = arbol.paradasDeCiudad(ciudad);
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        return paradas;
    }
}
