package transporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.ABORDAJES;
import static transporte.DatosDePrueba.CIUDAD;
import static transporte.DatosDePrueba.TRAMOS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import transporte.abordajes.ReporteAbordajes;

@DisplayName("RF2, RF3 y RNF2 - Comandos del programa (lo que ve el usuario)")
class MainTest {

    private static final class Resultado {
        final int codigo;
        final String salida;
        final String errores;

        Resultado(int codigo, String salida, String errores) {
            this.codigo = codigo;
            this.salida = salida;
            this.errores = errores;
        }
    }

    private Resultado ejecutar(Path abordajes, Path tramos, String... argumentos) {
        ByteArrayOutputStream bytesSalida = new ByteArrayOutputStream();
        ByteArrayOutputStream bytesErrores = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(bytesSalida, true, StandardCharsets.UTF_8);
        PrintStream errores = new PrintStream(bytesErrores, true, StandardCharsets.UTF_8);
        int codigo = Main.ejecutar(argumentos, salida, errores, abordajes, tramos);
        return new Resultado(codigo, bytesSalida.toString(StandardCharsets.UTF_8),
                bytesErrores.toString(StandardCharsets.UTF_8));
    }

    private Resultado ejecutar(String... argumentos) {
        return ejecutar(ABORDAJES, TRAMOS, argumentos);
    }

    // ------------------------------------------------------------ ruta (RF3)

    @Test
    @DisplayName("ruta Terminal Faro: muestra la ruta más corta y 15 minutos")
    void rutaExacta() {
        Resultado r = ejecutar("ruta", "Terminal", "Faro");
        assertEquals(0, r.codigo);
        assertEquals("Ruta más corta: Terminal -> Estadio -> Parque -> Faro. Tiempo estimado: 15 minutos.\n", r.salida);
        assertEquals("", r.errores);
    }

    @Test
    @DisplayName("ruta con origen igual al destino: 0 minutos")
    void rutaAlMismoLugar() {
        Resultado r = ejecutar("ruta", "Playa", "Playa");
        assertEquals(0, r.codigo);
        assertEquals("Ya se encuentra en la parada Playa. Tiempo estimado: 0 minutos.\n", r.salida);
    }

    @Test
    @DisplayName("ruta con empate de caminos: el tiempo es 7 minutos")
    void rutaConEmpate() {
        Resultado r = ejecutar("ruta", "Mercado", "Universidad");
        assertEquals(0, r.codigo);
        assertTrue(r.salida.startsWith("Ruta más corta: Mercado -> "));
        assertTrue(r.salida.endsWith("Universidad. Tiempo estimado: 7 minutos.\n"));
    }

    @Test
    @DisplayName("ruta con una parada que no existe: mensaje claro y código de error")
    void rutaParadaInexistente() {
        Resultado r = ejecutar("ruta", "Terminal", "Zoo");
        assertEquals(1, r.codigo);
        assertEquals("", r.salida);
        assertEquals("La parada 'Zoo' no existe.\n", r.errores);
    }

    @Test
    @DisplayName("ruta sin los dos datos: explica cómo se usa")
    void rutaSinDatos() {
        assertEquals("Uso: ruta <salida> <destino>\n", ejecutar("ruta", "Terminal").errores);
        assertEquals(1, ejecutar("ruta").codigo);
        assertEquals("Uso: ruta <salida> <destino>\n", ejecutar("ruta", "A", "B", "C").errores);
    }

    // ------------------------------------------------------------ zona (RF1)

    @Test
    @DisplayName("zona Norte: 2745 abordajes diarios")
    void zonaExacta() {
        Resultado r = ejecutar("zona", "Norte");
        assertEquals(0, r.codigo);
        assertEquals("Abordajes diarios de la zona Norte: 2745\n", r.salida);
    }

    @Test
    @DisplayName("zona inexistente o vacía: mensaje claro")
    void zonaInvalida() {
        assertEquals("La zona 'Sur' no está registrada en la ciudad 'Ciudad'.\n", ejecutar("zona", "Sur").errores);
        assertEquals("Debe indicar la zona.\n", ejecutar("zona", "").errores);
        assertEquals("Uso: zona <nombre de la zona>\n", ejecutar("zona").errores);
        assertEquals(1, ejecutar("zona", "Sur").codigo);
    }

    // ------------------------------------------------------------ reporte (RF2)

    @Test
    @DisplayName("reporte: es el mismo texto que genera ReporteAbordajes, de mayor a menor abordaje")
    void reporteDescendente() throws IOException {
        String esperado = ReporteAbordajes.generar(DatosDePrueba.arbolDesdeCsv(), CIUDAD);
        assertEquals(esperado, ejecutar("reporte").salida);
        assertEquals(0, ejecutar("reporte").codigo);
    }

    @Test
    @DisplayName("reporte con datos de más: explica cómo se usa")
    void reporteOpcionInvalida() {
        assertEquals("Uso: reporte\n", ejecutar("reporte", "otro").errores);
        assertEquals("Uso: reporte\n", ejecutar("reporte", "a", "b").errores);
        assertEquals(1, ejecutar("reporte", "otro").codigo);
    }

    // ------------------------------------------------------------ ayuda y errores generales (RNF2)

    @Test
    @DisplayName("Sin argumentos muestra los comandos disponibles")
    void sinArgumentos() {
        Resultado r = ejecutar();
        assertEquals(0, r.codigo);
        assertEquals(Main.ayuda(), r.salida);
        assertTrue(r.salida.contains("reporte  "));
        assertTrue(r.salida.contains("zona <nombre de la zona>"));
        assertTrue(r.salida.contains("ruta <salida> <destino>"));
    }

    @Test
    @DisplayName("Un comando desconocido se avisa y se muestra la ayuda")
    void comandoDesconocido() {
        Resultado r = ejecutar("hola");
        assertEquals(1, r.codigo);
        assertEquals("No se reconoce el comando 'hola'.\n" + Main.ayuda(), r.errores);
    }

    @Test
    @DisplayName("Si falta el archivo de datos, el mensaje explica qué revisar")
    void archivoInexistente() {
        Path falso = Path.of("datos", "no_existe.csv");
        String esperado = "No se pudo leer el archivo de datos. Revise que exista en la carpeta 'datos'.\n";
        assertEquals(esperado, ejecutar(falso, TRAMOS, "reporte").errores);
        assertEquals(esperado, ejecutar(ABORDAJES, falso, "ruta", "Terminal", "Faro").errores);
        assertEquals(1, ejecutar(falso, falso, "zona", "Norte").codigo);
    }

    @Test
    @DisplayName("Si un archivo tiene un dato mal escrito, el mensaje indica la línea")
    void archivoMalFormado(@TempDir Path carpeta) throws IOException {
        Path malo = Files.writeString(carpeta.resolve("tramos.csv"),
                "linea,desde,hacia,minutos\nBus 1,A,B,5\nBus 1,B,C,diez\n");
        Resultado r = ejecutar(ABORDAJES, malo, "ruta", "A", "C");
        assertEquals(1, r.codigo);
        assertEquals("Línea 3: el valor de 'minutos' debe ser un número entero, y se encontró 'diez'.\n", r.errores);
    }
}
