package transporte.ordenamiento;

import transporte.modelo.Parada;

/**
 * Criterio de comparación entre dos paradas.
 * Devuelve un valor negativo si {@code a} va antes que {@code b}, positivo si
 * va después y cero si son equivalentes para este criterio.
 */
@FunctionalInterface
public interface Criterio {

    int comparar(Parada a, Parada b);
}
