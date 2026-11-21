package transporte.grafo;

import transporte.modelo.Validacion;

/**
 * Grafo no dirigido y ponderado. Los vértices son paradas y cada arista es un
 * tramo con su tiempo en minutos. Se implementa solo con arreglos.
 */
public final class Grafo {

    private String[] paradas = new String[8];
    private int cantidadParadas;

    // Para cada parada: índices de sus vecinas y minutos hacia cada una.
    private int[][] vecinos = new int[8][];
    private int[][] minutos = new int[8][];
    private int[] grado = new int[8];

    private int cantidadTramos;

    public void agregarParada(String nombre) {
        Validacion.requerirTexto(nombre, "el nombre de la parada");
        if (indiceDe(nombre) >= 0) {
            throw new IllegalArgumentException("La parada '" + nombre + "' ya existe.");
        }
        if (cantidadParadas == paradas.length) {
            ampliar();
        }
        paradas[cantidadParadas] = nombre;
        vecinos[cantidadParadas] = new int[4];
        minutos[cantidadParadas] = new int[4];
        grado[cantidadParadas] = 0;
        cantidadParadas++;
    }

    /**
     * Une dos paradas existentes con un tramo en ambos sentidos.
     *
     * @param minutos tiempo del tramo; debe ser mayor que cero
     */
    public void agregarTramo(String desde, String hacia, int minutos) {
        Validacion.requerirTexto(desde, "la parada de origen del tramo");
        Validacion.requerirTexto(hacia, "la parada de destino del tramo");
        int a = indiceDe(desde);
        if (a < 0) {
            throw new IllegalArgumentException("La parada '" + desde + "' no existe.");
        }
        int b = indiceDe(hacia);
        if (b < 0) {
            throw new IllegalArgumentException("La parada '" + hacia + "' no existe.");
        }
        if (a == b) {
            throw new IllegalArgumentException(
                    "Un tramo no puede unir la parada '" + desde + "' consigo misma.");
        }
        if (minutos <= 0) {
            throw new IllegalArgumentException(
                    "El tiempo del tramo entre '" + desde + "' y '" + hacia + "' debe ser mayor que cero.");
        }
        agregarArista(a, b, minutos);
        agregarArista(b, a, minutos);
        cantidadTramos++;
    }

    public boolean existeParada(String nombre) {
        return indiceDe(nombre) >= 0;
    }

    public int cantidadParadas() {
        return cantidadParadas;
    }

    public int cantidadTramos() {
        return cantidadTramos;
    }

    /**
     * Minutos del tramo directo entre dos paradas (si hay varios, el menor).
     * Devuelve -1 si no hay un tramo directo.
     */
    public int minutosDirectos(String desde, String hacia) {
        int a = indiceDe(desde);
        int b = indiceDe(hacia);
        if (a < 0 || b < 0) {
            return -1;
        }
        int mejor = -1;
        for (int i = 0; i < grado[a]; i++) {
            if (vecinos[a][i] == b && (mejor < 0 || minutos[a][i] < mejor)) {
                mejor = minutos[a][i];
            }
        }
        return mejor;
    }

    // ------------------------------------------------- uso interno (Dijkstra)

    int indiceDe(String nombre) {
        if (nombre == null) {
            return -1;
        }
        for (int i = 0; i < cantidadParadas; i++) {
            if (paradas[i].equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    String nombreDe(int indice) {
        return paradas[indice];
    }

    int gradoDe(int indice) {
        return grado[indice];
    }

    int vecinoDe(int indice, int posicion) {
        return vecinos[indice][posicion];
    }

    int minutosHacia(int indice, int posicion) {
        return minutos[indice][posicion];
    }

    // ------------------------------------------------- privados

    private void agregarArista(int desde, int hacia, int tiempo) {
        if (grado[desde] == vecinos[desde].length) {
            int[] nuevosVecinos = new int[vecinos[desde].length * 2];
            int[] nuevosMinutos = new int[minutos[desde].length * 2];
            System.arraycopy(vecinos[desde], 0, nuevosVecinos, 0, grado[desde]);
            System.arraycopy(minutos[desde], 0, nuevosMinutos, 0, grado[desde]);
            vecinos[desde] = nuevosVecinos;
            minutos[desde] = nuevosMinutos;
        }
        vecinos[desde][grado[desde]] = hacia;
        minutos[desde][grado[desde]] = tiempo;
        grado[desde]++;
    }

    private void ampliar() {
        int nuevaCapacidad = paradas.length * 2;
        String[] nuevasParadas = new String[nuevaCapacidad];
        int[][] nuevosVecinos = new int[nuevaCapacidad][];
        int[][] nuevosMinutos = new int[nuevaCapacidad][];
        int[] nuevoGrado = new int[nuevaCapacidad];
        System.arraycopy(paradas, 0, nuevasParadas, 0, cantidadParadas);
        System.arraycopy(vecinos, 0, nuevosVecinos, 0, cantidadParadas);
        System.arraycopy(minutos, 0, nuevosMinutos, 0, cantidadParadas);
        System.arraycopy(grado, 0, nuevoGrado, 0, cantidadParadas);
        paradas = nuevasParadas;
        vecinos = nuevosVecinos;
        minutos = nuevosMinutos;
        grado = nuevoGrado;
    }
}
