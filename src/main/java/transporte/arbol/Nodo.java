package transporte.arbol;

import transporte.modelo.Parada;

/**
 * Nodo de un árbol N-ario. Cada nodo guarda sus hijos en un arreglo que crece
 * cuando se llena (no se usan colecciones de la biblioteca estándar).
 */
public final class Nodo {

    /** Nivel del árbol al que pertenece el nodo. */
    public enum Tipo {
        RAIZ, CIUDAD, ZONA, PARADA
    }

    private final Tipo tipo;
    private final String nombre;
    private final Parada parada;
    private Nodo[] hijos = new Nodo[4];
    private int cantidadHijos;

    Nodo(Tipo tipo, String nombre, Parada parada) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.parada = parada;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    /** Solo tiene valor en los nodos de tipo PARADA; en los demás es null. */
    public Parada getParada() {
        return parada;
    }

    public int getCantidadHijos() {
        return cantidadHijos;
    }

    public Nodo getHijo(int posicion) {
        if (posicion < 0 || posicion >= cantidadHijos) {
            throw new IllegalArgumentException(
                    "La posición " + posicion + " no existe entre los " + cantidadHijos + " hijos del nodo.");
        }
        return hijos[posicion];
    }

    void agregarHijo(Nodo hijo) {
        if (cantidadHijos == hijos.length) {
            Nodo[] ampliado = new Nodo[hijos.length * 2];
            System.arraycopy(hijos, 0, ampliado, 0, cantidadHijos);
            hijos = ampliado;
        }
        hijos[cantidadHijos] = hijo;
        cantidadHijos++;
    }

    /** Busca entre los hijos uno del tipo y nombre indicados; devuelve null si no hay. */
    Nodo buscarHijo(Tipo tipoBuscado, String nombreBuscado) {
        for (int i = 0; i < cantidadHijos; i++) {
            if (hijos[i].tipo == tipoBuscado && hijos[i].nombre.equals(nombreBuscado)) {
                return hijos[i];
            }
        }
        return null;
    }
}
