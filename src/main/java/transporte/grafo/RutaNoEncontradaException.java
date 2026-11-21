package transporte.grafo;

/**
 * Se lanza cuando las dos paradas existen pero no hay forma de llegar de una a la otra.
 */
public class RutaNoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RutaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
