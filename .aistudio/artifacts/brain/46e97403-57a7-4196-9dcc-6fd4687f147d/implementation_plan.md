# Plan de Implementación: Asignación de Áreas por Unidad y Corrección Visual de la Sábana Global

Este plan aborda las dos necesidades indicadas por el usuario:
1. **Asignación personalizada de áreas curriculares por unidad al importar el alumnado**: Cada unidad seleccionada contará con un selector/desplegable de áreas para permitir impartir diferentes materias en diferentes grupos.
2. **Corrección visual y estructural de la Sábana Global de Evaluación**: Sustituir el botón comprimido por una barra de acciones horizontal dedicada y espaciosa sobre la tabla matriz, asegurando que las medias de criterios y la media global de cada alumno se calculen y muestren con total legibilidad.

---

## 1. Asignación de Áreas por Unidad en la Importación (`ResumenScreen.kt` & `DocenteViewModel.kt`)

### Cambios en la UI (`DialogoSelectorUnidades`):
- Rediseñar `DialogoSelectorUnidades` para que cada unidad muestre:
  - Checkbox de activación de la unidad y nombre del grupo (ej. *1ºA*, *3ºB*).
  - Selector desplegable/expandible que muestra la lista de áreas oficiales (obtenidas de los criterios de evaluación: *Lengua Castellana y Literatura, Matemáticas, Conocimiento del Medio, Educación Física, Educación Artística, Lengua Extranjera (Inglés), Segunda Lengua Extranjera (Francés)*).
  - Checkboxes o chips interactivos para marcar qué áreas imparte el docente en esa unidad específica.
  - Opciones rápidas por unidad: "Todas las áreas" o marcar áreas individuales.

### Persistencia y Gestión en ViewModel (`DocenteViewModel.kt`):
- Almacenar el mapa `Map<String, List<String>>` (Unidad $\to$ Lista de Áreas seleccionadas) en la base de datos local / preferencias (`ConfiguracionApp` / `ConfiguracionDao`).
- Actualizar `areasEvaluacionDisponibles`:
  - Cuando se selecciona un grupo o unidad en la pestaña de Evaluación o en la creación de actividades, el selector de áreas filtrará automáticamente para mostrar **únicamente las áreas asignadas a esa unidad**.
  - Si una unidad no tiene áreas restringidas o es nueva, por defecto ofrece todas las áreas del currículo.

---

## 2. Rediseño de la Barra de Acciones y Sábana Global (`SeccionEvaluacion.kt`)

### Solución al problema de deformación visual:
- **Causa identificada**: En `SeccionEvaluacion.kt`, el texto descriptivo del grupo y el botón de PDF compartían una fila sin restricciones de peso (`Modifier.weight`), provocando que el texto empujara el botón contra el borde derecho y lo transformara en una tira vertical ilegible.
- **Nueva Barra de Acciones Horizontal**:
  - Implementar una barra o tarjeta horizontal (`Card` / `Surface`) estilizada con M3 sobre la tabla global.
  - Disposición clara:
    - Izquierda: Título *"Sábana Global de Criterios"* con insignia del grupo seleccionado y número de alumnos evaluados.
    - Derecha: Botón de acción con tamaño óptimo y padding táctil (mínimo 48dp) con icono `PictureAsPdf` y texto *"Informe PDF Global"*, garantizando que nunca se comprima.
  - Selector de áreas para la sábana global: permite ver la sábana global de un área concreta o de todas las asignadas al grupo.

### Renderizado de la Matriz Global (`TablaGlobalMatriz`):
- Columna fija inicial: Apellidos y Nombre de cada alumno (ordenado alfabéticamente según Séneca).
- Columnas intermedias con desplazamiento horizontal: cada criterio evaluado en las SdAs del curso con su media aritmética (ej. *CE.1.1*, *CE.1.2*...).
- Columna final destacada: **Media Global** de todas las calificaciones del alumno con código de color (aprobado/notable/sobresaliente/insuficiente).
- Fila inferior de resumen: Media global del grupo por criterio.

---

## 3. Verificación y Empaquetado

- Compilar con `compile_applet` para certificar la ausencia de errores de sintaxis y tipos en Kotlin y Jetpack Compose.
- Generar el APK actualizado mediante `./gradlew assembleDebug` y asegurar su copia como `cuaderno de zambrano.apk` y `Cuaderno de Zambrano.apk`.
- Verificar la navegación y el flujo de trabajo completo: Importación $\to$ Selección de Áreas por Unidad $\to$ Creación de Actividades $\to$ Visualización de la Sábana Global $\to$ Exportación a PDF y Séneca.
