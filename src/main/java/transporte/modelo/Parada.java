package transporte.modelo;

/**
 * Una parada del sistema: código, nombre, zona a la que pertenece y
 * cantidad de abordajes diarios.
 */
public final class Parada {

    private final String codigo;
    private final String nombre;
    private final String zona;
    private final int abordajes;

    public Parada(String codigo, String nombre, String zona, int abordajes) {
        Validacion.requerirTexto(codigo, "el código de la parada");
        Validacion.requerirTexto(nombre, "el nombre de la parada");
        Validacion.requerirTexto(zona, "la zona de la parada");
        if (abordajes < 0) {
            throw new IllegalArgumentException(
                    "Los abordajes de la parada " + codigo + " no pueden ser negativos.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.zona = zona;
        this.abordajes = abordajes;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getZona() {
        return zona;
    }

    public int getAbordajes() {
        return abordajes;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + zona + "): " + abordajes;
    }
}
