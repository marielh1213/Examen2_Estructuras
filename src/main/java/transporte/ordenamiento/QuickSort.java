package transporte.ordenamiento;

import transporte.modelo.Parada;

/**
 * QuickSort sobre arreglos de {@link Parada} con tiempo garantizado en el peor
 * de los casos: O(n log n).
 *
 * <p>Para garantizarlo, el pivote de cada partición es la mediana real del
 * tramo, obtenida con el método de la "mediana de medianas" (selección en
 * tiempo lineal). Así ninguna entrada (ordenada, inversa, con todos los
 * valores iguales, etc.) puede producir particiones desbalanceadas.
 * La partición se hace en tres zonas (menores, iguales, mayores) y se
 * recurre primero sobre la parte más pequeña, de modo que la profundidad de
 * la recursión es O(log n).</p>
 */
public final class QuickSort {

    private QuickSort() {
    }

    /** Ordena el arreglo en el sitio según el criterio indicado. */
    public static void ordenar(Parada[] paradas, Criterio criterio) {
        if (paradas == null) {
            throw new IllegalArgumentException("Debe indicar el arreglo de paradas a ordenar.");
        }
        if (criterio == null) {
            throw new IllegalArgumentException("Debe indicar el criterio de orden.");
        }
        for (int i = 0; i < paradas.length; i++) {
            if (paradas[i] == null) {
                throw new IllegalArgumentException(
                        "El arreglo de paradas tiene una posición vacía (posición " + i + ").");
            }
        }
        ordenar(paradas, 0, paradas.length - 1, criterio);
    }

    private static void ordenar(Parada[] a, int bajo, int alto, Criterio c) {
        while (bajo < alto) {
            int posMediana = seleccionar(a, bajo, alto, bajo + (alto - bajo) / 2, c);
            Parada pivote = a[posMediana];

            int menor = bajo;
            int mayor = alto;
            int i = bajo;
            while (i <= mayor) {
                int resultado = c.comparar(a[i], pivote);
                if (resultado < 0) {
                    intercambiar(a, menor, i);
                    menor++;
                    i++;
                } else if (resultado > 0) {
                    intercambiar(a, i, mayor);
                    mayor--;
                } else {
                    i++;
                }
            }
            // a[bajo..menor-1] < pivote, a[menor..mayor] == pivote, a[mayor+1..alto] > pivote
            if (menor - bajo < alto - mayor) {
                ordenar(a, bajo, menor - 1, c);
                bajo = mayor + 1;
            } else {
                ordenar(a, mayor + 1, alto, c);
                alto = menor - 1;
            }
        }
    }

    /**
     * Deja en la posición k el elemento que le corresponde si el tramo
     * a[bajo..alto] estuviera ordenado, y devuelve k. Tiempo lineal en el
     * peor caso (pivote por mediana de medianas).
     */
    private static int seleccionar(Parada[] a, int bajo, int alto, int k, Criterio c) {
        while (bajo < alto) {
            Parada pivote = a[pivoteMedianaDeMedianas(a, bajo, alto, c)];

            int menor = bajo;
            int mayor = alto;
            int i = bajo;
            while (i <= mayor) {
                int resultado = c.comparar(a[i], pivote);
                if (resultado < 0) {
                    intercambiar(a, menor, i);
                    menor++;
                    i++;
                } else if (resultado > 0) {
                    intercambiar(a, i, mayor);
                    mayor--;
                } else {
                    i++;
                }
            }
            if (k < menor) {
                alto = menor - 1;
            } else if (k > mayor) {
                bajo = mayor + 1;
            } else {
                return k;
            }
        }
        return bajo;
    }

    /** Devuelve la posición de un elemento que es la "mediana de las medianas de grupos de 5". */
    private static int pivoteMedianaDeMedianas(Parada[] a, int bajo, int alto, Criterio c) {
        int n = alto - bajo + 1;
        if (n <= 5) {
            ordenarGrupo(a, bajo, alto, c);
            return bajo + n / 2;
        }
        int grupos = 0;
        for (int inicio = bajo; inicio <= alto; inicio += 5) {
            int fin = Math.min(inicio + 4, alto);
            ordenarGrupo(a, inicio, fin, c);
            int mediana = inicio + (fin - inicio) / 2;
            intercambiar(a, bajo + grupos, mediana);
            grupos++;
        }
        return seleccionar(a, bajo, bajo + grupos - 1, bajo + (grupos - 1) / 2, c);
    }

    /** Ordena por inserción un grupo pequeño (máximo 5 elementos). */
    private static void ordenarGrupo(Parada[] a, int bajo, int alto, Criterio c) {
        for (int i = bajo + 1; i <= alto; i++) {
            Parada actual = a[i];
            int j = i - 1;
            while (j >= bajo && c.comparar(a[j], actual) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = actual;
        }
    }

    private static void intercambiar(Parada[] a, int i, int j) {
        Parada temporal = a[i];
        a[i] = a[j];
        a[j] = temporal;
    }
}
