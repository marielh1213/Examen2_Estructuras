# PROMPTS

Prompts enviados a la IA durante el desarrollo de este proyecto, en orden y sin modificar.

## Prompt 1

Archivos adjuntos: foto con la lista de requerimientos, `tramos.csv` y `abordajes.csv`.

```text
Necesito que en base a la foto proporcionada que son los requerimientos de mi proyecto, y a los archivos .csv, y únicamente en eso, sin hacer mejoras ni sugerir cambios, me ayudes a desempeñar lo siguiente Implementar pruebas unitarias que permitan comprobar cada
requerimiento. Las pruebas no deben limitarse a verificar verdadero/falso; por cada requerimiento funcional se debe incluir como mínimo:

* Una prueba que compare el valor exacto esperado (por ejemplo, assertEquals(11,

ruta.getMinutos())).

* Casos límite (por ejemplo, una zona o un ala vacía, origen igual al destino, empates).

* Entradas inválidas (por ejemplo, una parada o sala inexistente, o un dato mal formado), verificando el error o el resultado esperado.

o Se recomienda JUnit para Java, el repositorio base ya incluye ambos
configurados.
o Genera también un archivo README con las siguientes secciones:

* Nombre del examen-proyecto.
* Nombre de los autores.(Mariel Herrera, Luis Rodriguez)
* Requerimientos del sistema: la lista completa de requerimientos de la Parte 1, tal

como fueron presentados, sin modificaciones.(la de la foto)

* Instrucciones de instalación y ejecución, incluido cómo ejecutar las pruebas.
* Descripción de la jerarquía de carpetas del proyecto y de lo que hace cada módulo o clase.

3

* Requerimientos desarrollados por los estudiantes: cuáles requerimientos se implementaron y cuáles se excluyeron, con el motivo de cada exclusión.
* Diagrama de flujo, diagrama de componentes y diagrama del grafo que emplea el sistema, escritos en Mermaid dentro del README.

Lo que no puedes hacer: No debes sugerirme cambios o modificaciones, en los requerimientos ni puedes aplicar cambios en el codigo, debes apegarte a los requerimientos que te di.
Permitido: arreglos (por ejemplo int[] o Parada[]) y String.

* Prohibido en Java: cualquier colección de java.util (ArrayList, LinkedList, HashMap, HashSet, TreeMap, TreeSet, PriorityQueue, Stack, ArrayDeque, entre otras), así como Arrays.sort, Collections.sort, List.sort y sorted() de Streams. ordenamientos de LINQ (OrderBy, ThenBy).

* Prohibido: bibliotecas que calculen caminos más cortos o

que implementen grafos.

* Las pruebas unitarias pueden usar, además, la biblioteca de pruebas (JUnit ).
```

## Prompt 2

```text
Encontré una funcionalidad que no esta dentro de lo que puedes hacer en ordenar todo el reporte únicamente por código A–Z
En Main.java existe el comando: reporte codigo
Este utiliza OrdenListado.CODIGO_A_Z y Criterios.CODIGO_A_Z para ordenar todas las paradas por código, independientemente de sus abordajes.
Según los requerimientos que trabajamos, el código A–Z debía utilizarse para desempatar cuando dos paradas tenían la misma cantidad de abordajes.
Por tanto, esta opción de ordenamiento independiente no está justificada por ese requisito. Por lo que seria un requerimiento adicional y no puedes agregarme requerimientos adicionales. Podrías corregir eso? y por ultimo agregarme un archivo PROMPTS.md con todos los prompts que te he hecho.
```
