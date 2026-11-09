# Implementación: Renombrado a "Cuaderno de Zambrano" e Integración Google Drive

Actualización de la marca de la aplicación y configuración de OAuth 2.0 para acceso a Google Drive.

## Decisiones Críticas y Revisión del Usuario

> [!IMPORTANT]
> - **Cambio de Nombre**: Se renombrará la app en `metadata.json`, `strings.xml`, `settings.gradle.kts` y en todas las referencias del código (PDF, mensajes WhatsApp, tests) a "Cuaderno de Zambrano".
> - **Nuevo Icono**: Se utilizará `ic_teacher_planner.jpg` para generar el nuevo icono adaptativo de la app.
> - **Google Drive OAuth**: Implementaremos el flujo de OAuth 2.0 para que puedas vincular tu cuenta `g.educaand.es` y gestionar tus archivos CSV de forma segura en tu unidad de Drive.

---

## 1. Visión General y Concepto Principal

### Qué se entrega
1. **Renombrado Integral**: Cambio de "Planificador Docente" a "Cuaderno de Zambrano" en todos los archivos del proyecto, incluyendo el manifiesto, los recursos de texto y los mensajes generados por la app.
2. **Nuevo Icono Adaptativo**: Configuración del icono corporativo a partir de la imagen proporcionada.
3. **Integración con Google Drive**: Configuración segura de OAuth 2.0 para permitir el acceso a archivos de tu cuenta `g.educaand.es` y facilitar la importación/exportación de archivos CSV directamente a tu Drive.

---

## 2. Experiencia de Usuario y Diseño Visual

- **Nueva Identidad**: La app se identificará bajo la marca "Cuaderno de Zambrano".
- **Flujo de Integración**: Se añadirá un botón o acción en la pestaña "Importar Archivos" para vincular tu cuenta de Google. Una vez autorizado, se mostrará un selector de archivos de Google Drive para cargar directamente tus CSV.

---

## 3. Plan de Cambios Específicos

1. **Renombrado del Proyecto**:
   - Actualizar `metadata.json`, `strings.xml` y `settings.gradle.kts`.
   - Refactorizar las referencias en `ShareUtils.kt`, `PdfExporter.kt` y `ExampleUnitTest.kt`.
2. **Icono Adaptativo**:
   - Utilizar `app-icon-generation` para crear el nuevo icono a partir de `ic_teacher_planner.jpg`.
3. **Integración OAuth 2.0 (Google Drive)**:
   - Invocar el flujo `set_up_oauth` para solicitar acceso al scope `drive.file` (acceso limitado a archivos creados por la app).
   - Crear una interfaz en `SeccionImportacionArchivos` para activar la vinculación y abrir el selector de archivos de Drive (`ActivityResultContracts.PickVisualMedia` o API de Google Picker para archivos de Drive).
4. **Verificación**:
   - `compile_applet` y tests unitarios.
