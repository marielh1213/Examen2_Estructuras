package transporte.arbol;

import transporte.modelo.Parada;
import transporte.modelo.Validacion;

/**
 * Árbol N-ario que registra ciudades, zonas y paradas.
 *
 * <pre>
 * Sistema (raíz)
 *  └─ Ciudad
 *      └─ Zona
 *          └─ Parada
 * </pre>
 *
 * El código de una parada es único en todo el árbol.
 */
public final class ArbolNario {

    private final Nodo raiz = new Nodo(Nodo.Tipo.RAIZ, "Sistema", null);

    public Nodo getRaiz() {
        return raiz;
    }

    // ------------------------------------------------------------------ registro

    public void registrarCiudad(String ciudad) {
        Validacion.requerirTexto(ciudad, "la ciudad");
        if (raiz.buscarHijo(Nodo.Tipo.CIUDAD, ciudad) != null) {
            throw new IllegalArgumentException("La ciudad '" + ciudad + "' ya está registrada.");
        }
        raiz.agregarHijo(new Nodo(Nodo.Tipo.CIUDAD, ciudad, null));
    }

    public void registrarZona(String ciudad, String zona) {
        Nodo nodoCiudad = ciudadObligatoria(ciudad);
        Validacion.requerirTexto(zona, "la zona");
        if (nodoCiudad.buscarHijo(Nodo.Tipo.ZONA, zona) != null) {
            throw new IllegalArgumentException(
                    "La zona '" + zona + "' ya está registrada en la ciudad '" + ciudad + "'.");
        }
        nodoCiudad.agregarHijo(new Nodo(Nodo.Tipo.ZONA, zona, null));
    }

    public void registrarParada(String ciudad, String zona, String codigo, String nombre, int abordajes) {
        Nodo nodoZona = zonaObligatoria(ciudad, zona);
        Parada parada = new Parada(codigo, nombre, zona, abordajes);
        if (existeParada(codigo)) {
            throw new IllegalArgumentException("Ya existe una parada con el código '" + codigo + "'.");
        }
        nodoZona.agregarHijo(new Nodo(Nodo.Tipo.PARADA, codigo, parada));
    }

    // ------------------------------------------------------------------ consultas

    public boolean existeCiudad(String ciudad) {
        return ciudad != null && raiz.buscarHijo(Nodo.Tipo.CIUDAD, ciudad) != null;
    }

    public boolean existeZona(String ciudad, String zona) {
        if (!existeCiudad(ciudad) || zona == null) {
            return false;
        }
        return raiz.buscarHijo(Nodo.Tipo.CIUDAD, ciudad).buscarHijo(Nodo.Tipo.ZONA, zona) != null;
    }

    public boolean existeParada(String codigo) {
        return buscarNodoParada(codigo) != null;
    }

    /** Devuelve la parada con ese código; si no existe lanza IllegalArgumentException. */
    public Parada obtenerParada(String codigo) {
        Nodo nodo = buscarNodoParada(codigo);
        if (nodo == null) {
            throw new IllegalArgumentException("La parada con código '" + codigo + "' no existe.");
        }
        return nodo.getParada();
    }

    public int cantidadCiudades() {
        return raiz.getCantidadHijos();
    }

    public int cantidadZonas(String ciudad) {
        return ciudadObligatoria(ciudad).getCantidadHijos();
    }

    /** Cantidad de paradas de una zona. */
    public int cantidadParadas(String ciudad, String zona) {
        return zonaObligatoria(ciudad, zona).getCantidadHijos();
    }

    /** Cantidad de paradas de una ciudad (todas sus zonas). */
    public int cantidadParadas(String ciudad) {
        Nodo nodoCiudad = ciudadObligatoria(ciudad);
        int total = 0;
        for (int i = 0; i < nodoCiudad.getCantidadHijos(); i++) {
            total += nodoCiudad.getHijo(i).getCantidadHijos();
        }
        return total;
    }

    /** Cantidad de paradas de todo el árbol. */
    public int cantidadParadas() {
        int total = 0;
        for (int i = 0; i < raiz.getCantidadHijos(); i++) {
            total += cantidadParadas(raiz.getHijo(i).getNombre());
        }
        return total;
    }

    /** Nombres de las ciudades, en el orden en que se registraron. */
    public String[] ciudades() {
        String[] nombres = new String[raiz.getCantidadHijos()];
        for (int i = 0; i < nombres.length; i++) {
            nombres[i] = raiz.getHijo(i).getNombre();
        }
        return nombres;
    }

    /** Nombres de las zonas de una ciudad, en el orden en que se registraron. */
    public String[] zonas(String ciudad) {
        Nodo nodoCiudad = ciudadObligatoria(ciudad);
        String[] nombres = new String[nodoCiudad.getCantidadHijos()];
        for (int i = 0; i < nombres.length; i++) {
            nombres[i] = nodoCiudad.getHijo(i).getNombre();
        }
        return nombres;
    }

    /** Paradas de una zona, en el orden en que se registraron. */
    public Parada[] paradasDeZona(String ciudad, String zona) {
        Nodo nodoZona = zonaObligatoria(ciudad, zona);
        Parada[] paradas = new Parada[nodoZona.getCantidadHijos()];
        for (int i = 0; i < paradas.length; i++) {
            paradas[i] = nodoZona.getHijo(i).getParada();
        }
        return paradas;
    }

    /** Paradas de una ciudad (zona por zona), en el orden en que se registraron. */
    public Parada[] paradasDeCiudad(String ciudad) {
        Nodo nodoCiudad = ciudadObligatoria(ciudad);
        Parada[] paradas = new Parada[cantidadParadas(ciudad)];
        int posicion = 0;
        for (int z = 0; z < nodoCiudad.getCantidadHijos(); z++) {
            Nodo nodoZona = nodoCiudad.getHijo(z);
            for (int p = 0; p < nodoZona.getCantidadHijos(); p++) {
                paradas[posicion] = nodoZona.getHijo(p).getParada();
                posicion++;
            }
        }
        return paradas;
    }

    // ------------------------------------------------------------------ privados

    private Nodo ciudadObligatoria(String ciudad) {
        Validacion.requerirTexto(ciudad, "la ciudad");
        Nodo nodoCiudad = raiz.buscarHijo(Nodo.Tipo.CIUDAD, ciudad);
        if (nodoCiudad == null) {
            throw new IllegalArgumentException("La ciudad '" + ciudad + "' no está registrada.");
        }
        return nodoCiudad;
    }

    private Nodo zonaObligatoria(String ciudad, String zona) {
        Nodo nodoCiudad = ciudadObligatoria(ciudad);
        Validacion.requerirTexto(zona, "la zona");
        Nodo nodoZona = nodoCiudad.buscarHijo(Nodo.Tipo.ZONA, zona);
        if (nodoZona == null) {
            throw new IllegalArgumentException(
                    "La zona '" + zona + "' no está registrada en la ciudad '" + ciudad + "'.");
        }
        return nodoZona;
    }

    private Nodo buscarNodoParada(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (int c = 0; c < raiz.getCantidadHijos(); c++) {
            Nodo nodoCiudad = raiz.getHijo(c);
            for (int z = 0; z < nodoCiudad.getCantidadHijos(); z++) {
                Nodo encontrado = nodoCiudad.getHijo(z).buscarHijo(Nodo.Tipo.PARADA, codigo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
