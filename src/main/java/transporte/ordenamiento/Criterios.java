package transporte.ordenamiento;

/**
 * Criterio de orden usado por el listado de abordajes.
 */
public final class Criterios {

    private Criterios() {
    }

    /**
     * De mayor a menor cantidad de abordajes. Si hay empate, por código de
     * parada de la A a la Z.
     */
    public static final Criterio MAYOR_ABORDAJE_Y_CODIGO = (a, b) -> {
        if (a.getAbordajes() != b.getAbordajes()) {
            return a.getAbordajes() > b.getAbordajes() ? -1 : 1;
        }
        return a.getCodigo().compareTo(b.getCodigo());
    };
}
