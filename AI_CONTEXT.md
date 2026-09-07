# Contexto de IA para el Proyecto: Sanatorio 3D

Este archivo contiene el contexto técnico esencial, restricciones del entorno y decisiones de diseño para garantizar que cualquier modelo o agente de IA continúe el desarrollo sin romper la arquitectura ni cometer regresiones.

---

## 🎯 Visión del Proyecto
- **Género**: Terror psicológico y misterio en primera persona en 3D.
- **Entorno**: Centro psiquiátrico abandonado de los años 80 ("Sanatorio San Rafael").
- **Formato**: Aplicación móvil Android para ejecutarse **estrictamente en orientación horizontal** (*landscape*).
- **Mecánica Central**: Exploración con linterna de haz dinámico, tubos fluorescentes defectuosos con parpadeo procedural, recolección de expedientes clínicos y gestión de la cordura.

---

## ⚙️ Reglas Críticas para la IA

1. **Integración Políglota Obligatoria**:
   - No eliminar ni sustituir C++, Rust ni Lua por funciones simplificadas en Kotlin.
   - El código en C++ reside en `app/src/main/cpp/`.
   - El código en Lua es el **código oficial en C de Lua 5.4.7** sin wrappers de terceros, ubicado en `app/src/main/cpp/lua/`.
   - El código en Rust reside en `app/src/main/rust/` bajo el crate `asylum_core_rust`.

2. **Compilación de Rust para Android**:
   - El crate de Rust está configurado con `crate-type = ["staticlib"]` y `#![no_std]`.
   - Se debe mantener la función `#[no_mangle] pub extern "C" fn rust_eh_personality() {}` en `lib.rs` para evitar errores de enlace en arquitecturas de 32 bits (`armeabi-v7a`).
   - Los targets soportados son:
     - `aarch64-linux-android`
     - `armv7-linux-androideabi`
     - `x86_64-linux-android`

3. **Compilación de CMake**:
   - El archivo `CMakeLists.txt` enlaza directamente los 32 archivos `.c` de Lua 5.4.7 junto a `native_bridge.cpp` y el archivo estático `.a` generado por Cargo.
   - La librería compartida de salida es `libsanatorio_native.so`.

4. **Distribución de la Aplicación**:
   - El usuario distribuirá la app mediante APKs independientes o plataformas alternativas (como Uptodown), no a través de Google Play Store.
   - La prioridad absoluta es la funcionalidad y robustez de las librerías nativas y dependencias.

5. **Entorno Móvil del Usuario**:
   - El usuario trabaja directamente desde un teléfono móvil, por lo que las soluciones deben funcionar de forma autónoma en el contenedor y no requerir intervención manual compleja en línea de comandos.

6. **Modelos 3D y Recursos Tridimensionales**:
   - Ubicación estándar en el APK: `app/src/main/assets/models/`.
   - Formatos admitidos: glTF 2.0 (`.gltf` autocontenido o con buffers binarios y PBR) y Wavefront OBJ (`.obj` / `.mtl`).
   - Los modelos interactivos como la linterna poseen estados diferenciados (`flashlight_lights_on` vs `flashlight_lights_off`).
   - El script `./fetch_assets.sh` automatiza la descarga y procesamiento de modelos CC0 (Dominio Público) e inyecta jerarquías de armaduras/huesos (rigging).
   - Los modelos articulados (como `investigator_hands` de 8 articulaciones) disponen de un modelo de datos reactivo (`HandRigModel.kt`) sincronizado con la vista FPS y los controles deslizantes del cuaderno.
   - Siempre verificar que los modelos no utilicen marcas registradas y cumplan con las especificaciones estándar de glTF 2.0 y licencia CC0.

7. **Sin Dependencia de Archivos .env**:
   - La aplicación no requiere ni utiliza variables de entorno en archivos `.env` o `.env.example`.
   - El plugin `secrets-gradle-plugin` está desactivado para no añadir restricciones innecesarias a la compilación.

8. **Ubicación de Documentación y Mensajes de Commit**:
   - Todos los archivos `.md` (`README.md`, `ROADMAP.md`, `ESTRUCTURE.md`, `AI_CONTEXT.md`, `AGENTS.md`) deben residir exclusivamente en la raíz del repositorio, nunca dentro del subdirectorio `app/`.
   - Se puede ejecutar `./organize_md_to_root.sh` para limpiar y centralizar automáticamente cualquier archivo `.md`.
   - Si existe el archivo `commit_message.txt`, debe estar redactado íntegramente en español y solo actualizarse cuando el usuario lo solicite explícitamente. El flujo de GitHub Actions `override_commit_message.yml` lo utiliza para sincronizar el historial git.
