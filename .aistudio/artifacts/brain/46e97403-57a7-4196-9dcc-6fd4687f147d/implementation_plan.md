# Limpieza de Interfaz en Claustro y Privacidad de Archivos Adjuntos

Eliminación del botón redundante de subida de CSV en la cabecera de la pestaña Claustro (manteniendo toda la carga centralizada en *Importar Archivos*) y eliminación segura de los archivos temporales adjuntos en `/public/assets/aistudio/` para garantizar la máxima privacidad del centro escolar.

## Decisiones Críticas y Revisión del Usuario

> [!IMPORTANT]
> Confirmado por el usuario en la fase de clarificación:
> - **Botón redundante en Claustro**: Retirar el botón `Cargar RelPerCen.csv` de la pestaña Claustro. La carga de archivos queda unificada y organizada exclusivamente en la pestaña **Importar Archivos**.
> - **Archivos adjuntos en `/public/assets/aistudio/`**: Eliminar los archivos CSV subidos como adjuntos temporales en el chat (`RegAlum_f30f3699.csv`, `RelPerCen_0d6fa40d.csv`, `horario_64fbabee.csv`, etc.) para que no ocupen espacio ni figuren en el explorador de archivos.

---

## 1. Visión General y Concepto Principal

### Qué se entrega
1. **Pestaña Claustro Más Limpia y Enfocada**: En la cabecera de la pestaña Claustro se mantiene únicamente el recuento de profesores de la plantilla (ej. "34 profesores en plantilla") junto al buscador de docentes, retirando el botón de subida para no duplicar funciones con la pestaña *Importar Archivos*.
2. **Privacidad y Limpieza del Proyecto**: Eliminación física de los archivos temporales en `/public/assets/aistudio/`, asegurando que el espacio del proyecto quede libre de datos personales de centro y que solo residan en la base de datos SQLite local del dispositivo del usuario cuando este decida importarlos.

---

## 2. Experiencia de Usuario y Diseño Visual

### Cabecera de Claustro Actualizada

```
┌────────────────────────────────────────────────────────┐
│ LazyColumn (fillMaxSize)                               │
│                                                        │
│ [ 🔍 Buscar por nombre, apellidos o correo...       ]  │ <- Buscador
│                                                        │
│ [ 34 profesores en plantilla ]                         │ <- Solo el badge informativo
│                                                        │
│ ┌──────────────────────────────────────────────────┐   │
│ │ (M) Miguel de Cervantes                    [ ✉ ] │   │ <- Tarjetas de docentes
│ │     mcervantes@ejemplo.edu                       │   │
│ └──────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

## 3. Plan de Cambios Específicos

1. **`ResumenScreen.kt` (`SeccionClaustro`)**:
   - Eliminar el parámetro `onImportarClaustro` de `SeccionClaustro`.
   - En el `item(key = "header_count")`, mantener únicamente el `Surface` con el contador de profesores en plantilla, eliminando el `TextButton` de carga.
   - Si no hay profesores importados, el mensaje del estado vacío dirigirá al usuario a la pestaña *Importar Archivos*.
2. **Limpieza de Archivos**:
   - Eliminar los archivos CSV de `/public/assets/aistudio/`:
     - `RegAlum_f30f3699.csv`
     - `RelPerCen_0d6fa40d.csv`
     - `horario_64fbabee.csv`
     - `C_digo.gs_5fc91608.txt`
     - `Index_15b28775.html`
3. **Verificación**:
   - Compilación con `compile_applet`.
   - Ejecución de pruebas unitarias con `gradle :app:testDebugUnitTest`.
   - Re-ensamblado del APK (`gradle assembleDebug`) limpio y listo para su descarga.
