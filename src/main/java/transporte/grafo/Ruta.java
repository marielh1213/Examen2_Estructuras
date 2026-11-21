package transporte.grafo;

/**
 * Resultado de buscar la ruta más corta: las paradas por las que se pasa
 * (de la salida al destino) y el tiempo estimado en minutos.
 */
public final class Ruta {

    private final String[] paradas;
    private final int minutos;

    Ruta(String[] paradas, int minutos) {
        this.paradas = paradas;
        this.minutos = minutos;
    }

    /** Copia de las paradas de la ruta, empezando por la salida y terminando en el destino. */
    public String[] getParadas() {
        String[] copia = new String[paradas.length];
        System.arraycopy(paradas, 0, copia, 0, paradas.length);
        return copia;
    }

    public int getMinutos() {
        return minutos;
    }

    public int getCantidadParadas() {
        return paradas.length;
    }

    public String getOrigen() {
        return paradas[0];
    }

    public String getDestino() {
        return paradas[paradas.length - 1];
    }

    /** Texto sencillo para mostrar al pasajero. */
    public String descripcion() {
        String tiempo = minutos == 1 ? "1 minuto" : minutos + " minutos";
        if (paradas.length == 1) {
            return "Ya se encuentra en la parada " + paradas[0] + ". Tiempo estimado: " + tiempo + ".";
        }
        StringBuilder texto = new StringBuilder("Ruta más corta: ");
        for (int i = 0; i < paradas.length; i++) {
            if (i > 0) {
                texto.append(" -> ");
            }
            texto.append(paradas[i]);
        }
        texto.append(". Tiempo estimado: ").append(tiempo).append(".");
        return texto.toString();
    }
}
