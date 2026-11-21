package transporte.ordenamiento;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transporte.DatosDePrueba.assertError;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import transporte.DatosDePrueba;
import transporte.modelo.Parada;

@DisplayName("RF2 (req. 3, 4 y 5) - QuickSort: orden, desempate por código y peor caso garantizado")
class QuickSortTest {

    /** Criterio que cuenta cuántas comparaciones se hacen (mide el costo del ordenamiento). */
    private static final class ContadorDeComparaciones implements Criterio {
        private final Criterio base;
        private long comparaciones;

        ContadorDeComparaciones(Criterio base) {
            this.base = base;
        }

        @Override
        public int comparar(Parada a, Parada b) {
            comparaciones++;
            return base.comparar(a, b);
        }
    }

    /** Las 8 paradas de abordajes.csv, en el orden del archivo. */
    private Parada[] paradasDelCsv() {
        return new Parada[] {
            new Parada("S01", "Terminal", "Centro", 1520),
            new Parada("S02", "Mercado", "Centro", 980),
            new Parada("S03", "Universidad", "Norte", 1310),
            new Parada("S04", "Faro", "Costa", 640),
            new Parada("S05", "Estadio", "Norte", 980),
            new Parada("S06", "Parque", "Norte", 455),
            new Parada("S07", "Playa", "Costa", 640),
            new Parada("S08", "Hospital", "Centro", 1310)
        };
    }

    private void verificarOrden(Parada[] resultado, Criterio criterio) {
        for (int i = 1; i < resultado.length; i++) {
            assertTrue(criterio.comparar(resultado[i - 1], resultado[i]) <= 0,
                    "fuera de orden entre las posiciones " + (i - 1) + " y " + i);
        }
    }

    /** Cada parada del original aparece exactamente una vez en el resultado (misma referencia). */
    private void verificarPermutacion(Parada[] original, Parada[] resultado) {
        assertEquals(original.length, resultado.length);
        boolean[] usada = new boolean[resultado.length];
        for (int i = 0; i < original.length; i++) {
            boolean encontrada = false;
            for (int j = 0; j < resultado.length && !encontrada; j++) {
                if (!usada[j] && resultado[j] == original[i]) {
                    usada[j] = true;
                    encontrada = true;
                }
            }
            assertTrue(encontrada, "se perdió la parada " + original[i].getCodigo());
        }
    }

    private Parada[] copia(Parada[] paradas) {
        Parada[] copia = new Parada[paradas.length];
        System.arraycopy(paradas, 0, copia, 0, paradas.length);
        return copia;
    }

    // ------------------------------------------------------------ valores exactos

    @Test
    @DisplayName("Req. 3: ordena de mayor a menor abordaje (valores exactos del CSV)")
    void ordenDeMayorAMenor() {
        Parada[] paradas = paradasDelCsv();
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);

        assertArrayEquals(new int[] {1520, 1310, 1310, 980, 980, 640, 640, 455}, DatosDePrueba.abordajes(paradas));
    }

    @Test
    @DisplayName("Req. 4: los empates se resuelven por código de parada de la A a la Z")
    void empatesPorCodigo() {
        Parada[] paradas = paradasDelCsv();
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);

        // Empates: 1310 -> S03, S08; 980 -> S02, S05; 640 -> S04, S07
        assertArrayEquals(new String[] {"S01", "S03", "S08", "S02", "S05", "S04", "S07", "S06"},
                DatosDePrueba.codigos(paradas));
    }

    @Test
    @DisplayName("Req. 4: empate entre tres paradas con el mismo abordaje")
    void empateDeTres() {
        Parada[] paradas = {
            new Parada("C", "Tercera", "Z", 50),
            new Parada("A", "Primera", "Z", 50),
            new Parada("B", "Segunda", "Z", 50)
        };
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new String[] {"A", "B", "C"}, DatosDePrueba.codigos(paradas));
    }

    @Test
    @DisplayName("Req. 4: un empate no altera la posición de las paradas con otros abordajes")
    void empateEnElMedio() {
        Parada[] paradas = {
            new Parada("Z9", "a", "Z", 10),
            new Parada("B1", "b", "Z", 30),
            new Parada("A1", "c", "Z", 20),
            new Parada("A2", "d", "Z", 20),
            new Parada("M5", "e", "Z", 40)
        };
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new String[] {"M5", "B1", "A1", "A2", "Z9"}, DatosDePrueba.codigos(paradas));
    }

    @Test
    @DisplayName("Ordena en el sitio: devuelve el mismo arreglo con las mismas paradas")
    void ordenaEnElSitio() {
        Parada[] paradas = paradasDelCsv();
        Parada[] original = copia(paradas);
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        verificarPermutacion(original, paradas);
    }

    // ------------------------------------------------------------ 1,200 paradas

    @Test
    @DisplayName("Req. 3: 1,200 paradas con abordajes distintos, de mayor (1200) a menor (1)")
    void milDoscientasDistintas() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = i + 1;
        }
        Parada[] paradas = DatosDePrueba.paradasConAbordajes(abordajes);
        Parada[] original = copia(paradas);
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);

        assertEquals(1200, paradas.length);
        assertEquals("P1200", paradas[0].getCodigo());
        assertEquals(1200, paradas[0].getAbordajes());
        assertEquals("P0600", paradas[600].getCodigo());
        assertEquals("P0001", paradas[1199].getCodigo());
        assertEquals(1, paradas[1199].getAbordajes());
        verificarOrden(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        verificarPermutacion(original, paradas);
    }

    @Test
    @DisplayName("Req. 3 y 4: 1,200 paradas con muchos empates (abordajes = i % 100)")
    void milDoscientasConEmpates() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = (i + 1) % 100;
        }
        Parada[] paradas = DatosDePrueba.paradasConAbordajes(abordajes);
        Parada[] original = copia(paradas);
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);

        // 99 abordajes: paradas 99, 199, ..., 1199 (12 empatadas), por código de A a Z
        assertArrayEquals(
                new String[] {"P0099", "P0199", "P0299", "P0399", "P0499", "P0599",
                              "P0699", "P0799", "P0899", "P0999", "P1099", "P1199"},
                DatosDePrueba.codigos(sub(paradas, 0, 12)));
        // 0 abordajes: paradas 100, 200, ..., 1200 (12 empatadas), al final de la lista
        assertArrayEquals(
                new String[] {"P0100", "P0200", "P0300", "P0400", "P0500", "P0600",
                              "P0700", "P0800", "P0900", "P1000", "P1100", "P1200"},
                DatosDePrueba.codigos(sub(paradas, 1188, 1200)));
        verificarOrden(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        verificarPermutacion(original, paradas);
    }

    private Parada[] sub(Parada[] paradas, int desde, int hasta) {
        Parada[] parte = new Parada[hasta - desde];
        System.arraycopy(paradas, desde, parte, 0, parte.length);
        return parte;
    }

    // ------------------------------------------------------------ req. 5: peor caso garantizado

    /** Entradas que hacen quedar mal a un QuickSort corriente (pivote fijo o de 3 puntos). */
    private int[][] entradasDificiles(int n) {
        int[] ascendente = new int[n];
        int[] descendente = new int[n];
        int[] iguales = new int[n];
        int[] organo = new int[n];
        int[] sierra = new int[n];
        int[] paridad = new int[n];
        for (int i = 0; i < n; i++) {
            ascendente[i] = i + 1;
            descendente[i] = n - i;
            iguales[i] = 5;
            organo[i] = i < n / 2 ? i + 1 : n - i;
            sierra[i] = i % 50;
            paridad[i] = i % 2;
        }
        return new int[][] {ascendente, descendente, iguales, organo, sierra, paridad};
    }

    private long comparacionesPara(int[] abordajes) {
        Parada[] paradas = DatosDePrueba.paradasConAbordajes(abordajes);
        ContadorDeComparaciones contador = new ContadorDeComparaciones(Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        QuickSort.ordenar(paradas, contador);
        verificarOrden(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        return contador.comparaciones;
    }

    @Test
    @DisplayName("Req. 5: con 1,200 paradas, ninguna entrada difícil supera el límite de comparaciones")
    void peorCasoAcotado() {
        int n = 1200;
        // Un QuickSort con pivote fijo necesita ~n^2/2 = 720,000 comparaciones en estas entradas.
        // Con pivote por mediana de medianas el costo queda en el orden de n log n
        // (n log2 n = 12,274; el peor caso medido fue 73,885).
        long limite = 100000;
        int[][] entradas = entradasDificiles(n);
        for (int i = 0; i < entradas.length; i++) {
            long comparaciones = comparacionesPara(entradas[i]);
            assertTrue(comparaciones <= limite,
                    "la entrada " + i + " usó " + comparaciones + " comparaciones (límite " + limite + ")");
        }
    }

    @Test
    @DisplayName("Req. 5: al duplicar las paradas el costo no se cuadruplica (crecimiento n log n)")
    void elCostoNoCrecePorEncimaDeNLogN() {
        int[][] pequenas = entradasDificiles(1200);
        int[][] grandes = entradasDificiles(2400);
        for (int i = 0; i < pequenas.length; i++) {
            long con1200 = comparacionesPara(pequenas[i]);
            long con2400 = comparacionesPara(grandes[i]);
            // n log n: la razón es ~2.1. Un algoritmo cuadrático daría ~4.
            assertTrue(con2400 * 10 < con1200 * 26,
                    "la entrada " + i + " pasó de " + con1200 + " a " + con2400 + " comparaciones");
        }
    }

    @Test
    @DisplayName("Req. 5: entradas difíciles de 1,200 paradas quedan bien ordenadas y completas")
    void entradasDificilesQuedanBienOrdenadas() {
        int[][] entradas = entradasDificiles(1200);
        for (int i = 0; i < entradas.length; i++) {
            Parada[] paradas = DatosDePrueba.paradasConAbordajes(entradas[i]);
            Parada[] original = copia(paradas);
            QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
            verificarOrden(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
            verificarPermutacion(original, paradas);
        }
    }

    // ------------------------------------------------------------ casos límite

    @Test
    @DisplayName("Límite: arreglo vacío")
    void vacio() {
        Parada[] paradas = new Parada[0];
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertEquals(0, paradas.length);
    }

    @Test
    @DisplayName("Límite: una sola parada")
    void unaParada() {
        Parada unica = new Parada("S01", "Terminal", "Centro", 10);
        Parada[] paradas = {unica};
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertSame(unica, paradas[0]);
    }

    @Test
    @DisplayName("Límite: dos paradas en orden inverso y dos ya ordenadas")
    void dosParadas() {
        Parada[] alReves = {new Parada("A", "a", "Z", 1), new Parada("B", "b", "Z", 9)};
        QuickSort.ordenar(alReves, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new String[] {"B", "A"}, DatosDePrueba.codigos(alReves));

        Parada[] ordenadas = {new Parada("B", "b", "Z", 9), new Parada("A", "a", "Z", 1)};
        QuickSort.ordenar(ordenadas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new String[] {"B", "A"}, DatosDePrueba.codigos(ordenadas));
    }

    @Test
    @DisplayName("Límite: todas las paradas con el mismo abordaje quedan por código de A a Z")
    void todasEmpatadas() {
        int[] abordajes = new int[1200];
        for (int i = 0; i < abordajes.length; i++) {
            abordajes[i] = 77;
        }
        Parada[] paradas = DatosDePrueba.paradasConAbordajes(abordajes);
        // se invierte el arreglo para que la entrada no esté ya ordenada por código
        for (int i = 0; i < paradas.length / 2; i++) {
            Parada temporal = paradas[i];
            paradas[i] = paradas[paradas.length - 1 - i];
            paradas[paradas.length - 1 - i] = temporal;
        }
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertEquals("P0001", paradas[0].getCodigo());
        assertEquals("P0002", paradas[1].getCodigo());
        assertEquals("P1200", paradas[1199].getCodigo());
    }

    @Test
    @DisplayName("Límite: paradas idénticas para el criterio (mismo código y abordajes) no se pierden")
    void paradasEquivalentes() {
        Parada[] paradas = {
            new Parada("S01", "Uno", "Z", 5),
            new Parada("S01", "Dos", "Z", 5),
            new Parada("S01", "Tres", "Z", 5),
            new Parada("S00", "Cuatro", "Z", 5),
            new Parada("S02", "Cinco", "Z", 9)
        };
        Parada[] original = copia(paradas);
        QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        verificarPermutacion(original, paradas);
        assertEquals("S02", paradas[0].getCodigo());
        assertEquals("S00", paradas[1].getCodigo());
    }

    @Test
    @DisplayName("Límite: ya ordenada y en orden inverso con pocas paradas")
    void yaOrdenadaEInversa() {
        Parada[] ordenada = DatosDePrueba.paradasConAbordajes(new int[] {5, 4, 3, 2, 1});
        QuickSort.ordenar(ordenada, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new int[] {5, 4, 3, 2, 1}, DatosDePrueba.abordajes(ordenada));

        Parada[] inversa = DatosDePrueba.paradasConAbordajes(new int[] {1, 2, 3, 4, 5});
        QuickSort.ordenar(inversa, Criterios.MAYOR_ABORDAJE_Y_CODIGO);
        assertArrayEquals(new int[] {5, 4, 3, 2, 1}, DatosDePrueba.abordajes(inversa));
        assertArrayEquals(new String[] {"P0005", "P0004", "P0003", "P0002", "P0001"}, DatosDePrueba.codigos(inversa));
    }

    // ------------------------------------------------------------ entradas inválidas

    @Test
    @DisplayName("Inválido: arreglo nulo")
    void arregloNulo() {
        assertError("Debe indicar el arreglo de paradas a ordenar.",
                () -> QuickSort.ordenar(null, Criterios.MAYOR_ABORDAJE_Y_CODIGO));
    }

    @Test
    @DisplayName("Inválido: criterio nulo")
    void criterioNulo() {
        assertError("Debe indicar el criterio de orden.", () -> QuickSort.ordenar(paradasDelCsv(), null));
    }

    @Test
    @DisplayName("Inválido: el arreglo tiene una posición vacía (no se modifica)")
    void posicionVacia() {
        Parada[] paradas = {new Parada("A", "a", "Z", 1), new Parada("B", "b", "Z", 2), null};
        assertError("El arreglo de paradas tiene una posición vacía (posición 2).",
                () -> QuickSort.ordenar(paradas, Criterios.MAYOR_ABORDAJE_Y_CODIGO));
        assertEquals("A", paradas[0].getCodigo());
        assertEquals("B", paradas[1].getCodigo());
    }
}
