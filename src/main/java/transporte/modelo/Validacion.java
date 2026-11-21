package transporte.modelo;

/**
 * Validaciones de texto compartidas por todo el sistema.
 * Los mensajes de error están escritos en lenguaje sencillo para el usuario.
 */
public final class Validacion {

    private Validacion() {
    }

    /**
     * Verifica que el texto no sea nulo ni esté vacío.
     *
     * @param valor texto recibido
     * @param que   descripción del dato, por ejemplo "el código de la parada"
     * @throws IllegalArgumentException con el mensaje "Debe indicar {que}."
     */
    public static void requerirTexto(String valor, String que) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe indicar " + que + ".");
        }
    }
}
