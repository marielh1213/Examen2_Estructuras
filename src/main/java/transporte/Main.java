package transporte;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import transporte.abordajes.ContadorAbordajes;
import transporte.abordajes.ReporteAbordajes;
import transporte.arbol.ArbolNario;
import transporte.datos.CargadorCsv;
import transporte.grafo.Dijkstra;
import transporte.grafo.Grafo;
import transporte.grafo.RutaNoEncontradaException;

/**
 * Punto de entrada por línea de comandos.
 *
 * <pre>
 * reporte
 * zona &lt;nombre de la zona&gt;
 * ruta &lt;salida&gt; &lt;destino&gt;
 * </pre>
 */
public final class Main {

    /** El CSV de abordajes no trae ciudad; se registra bajo este nombre. */
    public static final String CIUDAD = "Ciudad";

    public static final Path ARCHIVO_ABORDAJES = Path.of("datos", "abordajes.csv");
    public static final Path ARCHIVO_TRAMOS = Path.of("datos", "tramos.csv");

    private Main() {
    }

    public static void main(String[] args) {
        int codigo = ejecutar(args, System.out, System.err, ARCHIVO_ABORDAJES, ARCHIVO_TRAMOS);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    /**
     * Ejecuta un comando y devuelve 0 si salió bien, 1 si hubo un error.
     * El texto de salida usa siempre "\n" como salto de línea.
     */
    public static int ejecutar(String[] args, PrintStream salida, PrintStream errores,
                               Path archivoAbordajes, Path archivoTramos) {
        if (args.length == 0) {
            salida.print(ayuda());
            return 0;
        }
        try {
            switch (args[0]) {
                case "reporte":
                    return reporte(args, salida, errores, archivoAbordajes);
                case "zona":
                    return zona(args, salida, errores, archivoAbordajes);
                case "ruta":
                    return ruta(args, salida, errores, archivoTramos);
                default:
                    errores.print("No se reconoce el comando '" + args[0] + "'.\n" + ayuda());
                    return 1;
            }
        } catch (IllegalArgumentException | RutaNoEncontradaException e) {
            errores.print(e.getMessage() + "\n");
            return 1;
        } catch (IOException e) {
            errores.print("No se pudo leer el archivo de datos. Revise que exista en la carpeta 'datos'.\n");
            return 1;
        }
    }

    private static int reporte(String[] args, PrintStream salida, PrintStream errores, Path archivoAbordajes)
            throws IOException {
        if (args.length != 1) {
            errores.print("Uso: reporte\n");
            return 1;
        }
        salida.print(ReporteAbordajes.generar(cargarArbol(archivoAbordajes), CIUDAD));
        return 0;
    }

    private static int zona(String[] args, PrintStream salida, PrintStream errores, Path archivoAbordajes)
            throws IOException {
        if (args.length != 2) {
            errores.print("Uso: zona <nombre de la zona>\n");
            return 1;
        }
        long total = ContadorAbordajes.totalDeZona(cargarArbol(archivoAbordajes), CIUDAD, args[1]);
        salida.print("Abordajes diarios de la zona " + args[1] + ": " + total + "\n");
        return 0;
    }

    private static int ruta(String[] args, PrintStream salida, PrintStream errores, Path archivoTramos)
            throws IOException {
        if (args.length != 3) {
            errores.print("Uso: ruta <salida> <destino>\n");
            return 1;
        }
        Grafo grafo = CargadorCsv.cargarTramos(archivoTramos);
        salida.print(Dijkstra.rutaMasCorta(grafo, args[1], args[2]).descripcion() + "\n");
        return 0;
    }

    private static ArbolNario cargarArbol(Path archivoAbordajes) throws IOException {
        ArbolNario arbol = new ArbolNario();
        CargadorCsv.cargarAbordajes(archivoAbordajes, CIUDAD, arbol);
        return arbol;
    }

    static String ayuda() {
        return "Comandos disponibles:\n"
                + "  reporte                      Reporte de abordajes diarios, de mayor a menor abordaje (empates por código de A a Z)\n"
                + "  zona <nombre de la zona>     Abordajes diarios de una zona\n"
                + "  ruta <salida> <destino>      Ruta más corta y tiempo estimado\n";
    }
}
