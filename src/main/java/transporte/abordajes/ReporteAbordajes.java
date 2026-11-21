package transporte.abordajes;

import transporte.arbol.ArbolNario;
import transporte.modelo.Parada;

/**
 * Genera el reporte de abordajes diarios: total por zona y listado de paradas
 * ordenado. Todo el texto está pensado para que el usuario lo entienda sin
 * conocimientos técnicos.
 */
public final class ReporteAbordajes {

    private ReporteAbordajes() {
    }

    /** Reporte con el listado de mayor a menor abordaje (empates por código de A a Z). */
    public static String generar(ArbolNario arbol, String ciudad) {
        Parada[] listado = ListadoAbordajes.generar(arbol, ciudad);

        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE ABORDAJES DIARIOS\n");
        reporte.append("Ciudad: ").append(ciudad).append("\n");
        reporte.append("Orden del listado: de mayor a menor abordaje; si hay empate, por código de parada de la A a la Z.\n");
        reporte.append("\n");

        if (listado.length == 0) {
            reporte.append("No hay paradas registradas en esta ciudad.\n");
            return reporte.toString();
        }

        reporte.append("Abordajes por zona:\n");
        String[] zonas = arbol.zonas(ciudad);
        for (int i = 0; i < zonas.length; i++) {
            long total = ContadorAbordajes.totalDeZona(arbol, ciudad, zonas[i]);
            reporte.append("- ").append(zonas[i]).append(": ").append(textoAbordajes(total)).append("\n");
        }

        reporte.append("\n");
        reporte.append("Listado de paradas:\n");
        for (int i = 0; i < listado.length; i++) {
            Parada p = listado[i];
            reporte.append(i + 1).append(". ")
                    .append(p.getCodigo()).append(" - ").append(p.getNombre())
                    .append(" (").append(p.getZona()).append("): ")
                    .append(textoAbordajes(p.getAbordajes())).append("\n");
        }

        reporte.append("\n");
        reporte.append("Total de paradas: ").append(listado.length).append("\n");
        return reporte.toString();
    }

    private static String textoAbordajes(long cantidad) {
        return cantidad == 1 ? "1 abordaje" : cantidad + " abordajes";
    }
}
