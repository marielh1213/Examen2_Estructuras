package transporte.grafo;

import transporte.modelo.Validacion;

/**
 * Algoritmo de Dijkstra sobre un {@link Grafo} no dirigido y ponderado.
 * Se resuelve solo con arreglos (sin colas de prioridad de la biblioteca): en cada paso se toma la
 * parada pendiente con menor distancia acumulada.
 */
public final class Dijkstra {

    private static final long INFINITO = Long.MAX_VALUE;

    private Dijkstra() {
    }

    /**
     * Calcula la ruta más corta (en minutos) entre dos paradas.
     *
     * @throws IllegalArgumentException   si falta un dato o una de las paradas no existe
     * @throws RutaNoEncontradaException si no hay forma de llegar al destino
     */
    public static Ruta rutaMasCorta(Grafo grafo, String origen, String destino) {
        if (grafo == null) {
            throw new IllegalArgumentException("Debe indicar el mapa de paradas.");
        }
        Validacion.requerirTexto(origen, "la parada de salida");
        Validacion.requerirTexto(destino, "la parada de destino");
        int inicio = grafo.indiceDe(origen);
        if (inicio < 0) {
            throw new IllegalArgumentException("La parada '" + origen + "' no existe.");
        }
        int fin = grafo.indiceDe(destino);
        if (fin < 0) {
            throw new IllegalArgumentException("La parada '" + destino + "' no existe.");
        }

        int n = grafo.cantidadParadas();
        long[] distancia = new long[n];
        int[] anterior = new int[n];
        boolean[] definitiva = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancia[i] = INFINITO;
            anterior[i] = -1;
        }
        distancia[inicio] = 0;

        for (int paso = 0; paso < n; paso++) {
            int actual = -1;
            for (int v = 0; v < n; v++) {
                if (!definitiva[v] && distancia[v] != INFINITO
                        && (actual < 0 || distancia[v] < distancia[actual])) {
                    actual = v;
                }
            }
            if (actual < 0 || actual == fin) {
                break;
            }
            definitiva[actual] = true;
            for (int e = 0; e < grafo.gradoDe(actual); e++) {
                int vecino = grafo.vecinoDe(actual, e);
                long nueva = distancia[actual] + grafo.minutosHacia(actual, e);
                if (!definitiva[vecino] && nueva < distancia[vecino]) {
                    distancia[vecino] = nueva;
                    anterior[vecino] = actual;
                }
            }
        }

        if (distancia[fin] == INFINITO) {
            throw new RutaNoEncontradaException(
                    "No hay una ruta para ir de '" + origen + "' a '" + destino + "'.");
        }

        int cantidad = 1;
        for (int v = fin; anterior[v] >= 0; v = anterior[v]) {
            cantidad++;
        }
        String[] recorrido = new String[cantidad];
        int posicion = cantidad - 1;
        for (int v = fin; v >= 0; v = anterior[v]) {
            recorrido[posicion] = grafo.nombreDe(v);
            posicion--;
        }
        return new Ruta(recorrido, Math.toIntExact(distancia[fin]));
    }
}
