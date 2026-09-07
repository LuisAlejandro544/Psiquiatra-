# Contexto de IA para el Proyecto: Sanatorio 3D

Este archivo contiene el contexto técnico esencial, restricciones del entorno y decisiones de diseño para garantizar que cualquier modelo o agente de IA continúe el desarrollo sin romper la arquitectura ni cometer regresiones.

---

## 🎯 Visión del Proyecto
- **Género**: Terror psicológico y misterio en primera persona en 3D.
- **Entorno**: Centro psiquiátrico abandonado de los años 80 ("Sanatorio San Rafael").
- **Formato**: Aplicación móvil Android para ejecutarse **estrictamente en orientación horizontal** (*landscape*).
- **Mecánica Central**: Exploración con linterna vintage de haz volumétrico lógico, tubos fluorescentes defectuosos con parpadeo procedural, recolección de expedientes clínicos y gestión matemática de la cordura.

---

## ⚙️ Reglas Críticas para la IA

1. **Integración Políglota y GPU Obligatoria**:
   - No eliminar ni sustituir C++, Rust, Lua ni OpenGL ES 3.2 por funciones simplificadas en Kotlin.
   - El renderizado 3D principal se ejecuta a través de **OpenGL ES 3.2** en un hilo nativo de renderizado (`GLThread`) mediante `SanatorioGLSurfaceView` y `SanatorioGLRenderer`. Queda estrictamente prohibido volver a un bucle de software en CPU con `Bitmap` sobre el hilo de Compose, ya que provoca lag severo y degradación de FPS.
   - El código en C++ reside en `app/src/main/cpp/` enlazado con `GLESv3`, `EGL`, `log` y la librería estática de Rust.
   - El código en Lua es el **código oficial en C de Lua 5.4.7** sin wrappers de terceros, ubicado en `app/src/main/cpp/lua/`.
   - El código en Rust reside en `app/src/main/rust/` bajo el crate `asylum_core_rust`.

2. **Compilación de Rust para Android**:
   - El crate de Rust está configurado con `crate-type = ["staticlib"]` y `#![no_std]`.
   - Se debe mantener la función `#[no_mangle] pub extern "C" fn rust_eh_personality() {}` en `lib.rs` para evitar errores de enlace en arquitecturas de 32 bits (`armeabi-v7a`).
   - Los targets soportados son: `aarch64-linux-android`, `armv7-linux-androideabi` y `x86_64-linux-android`.

3. **Compilación de CMake y OpenGL ES**:
   - El archivo `CMakeLists.txt` enlaza los 32 archivos `.c` de Lua 5.4.7 junto a `native_bridge.cpp`, la librería estática de Rust y las librerías nativas de GPU `GLESv3` y `EGL`.
   - La librería compartida generada es `libsanatorio_native.so`.

4. **Distribución de la Aplicación**:
   - El usuario distribuirá la app mediante APKs independientes o plataformas alternativas (como Uptodown), no a través de Google Play Store.
   - La prioridad absoluta es la funcionalidad y robustez de las librerías nativas y dependencias.

5. **Entorno Móvil del Usuario**:
   - El usuario trabaja directamente desde un teléfono móvil, por lo que las soluciones deben compilar y funcionar de forma autónoma en el contenedor y no requerir intervención manual compleja en línea de comandos.

6. **Modelos 3D y Texturas PBR (CC0)**:
   - Ubicación estándar en el APK: `app/src/main/assets/models/` y `app/src/main/assets/textures/`.
   - Texturas de suelo: `dirty_tiles` e `interior_tiles` con mapas PBR (difuso, normales, rugosidad, ARM) gestionadas mediante `TextureLoader.kt`.
   - Linterna del juego: modelo `vintage_flashlight` con caja de batería roja, reflector pulido de aluminio, lente estriada, efecto de incandescencia gradual (`filamentWarmup`) y haz volumétrico en niebla.
   - Manos del investigador: modelo `investigator_hands` con rig de 8 articulaciones y guantes de cuero de investigación.
   - El script `./fetch_assets.sh` automatiza la descarga y procesamiento de modelos CC0 e inyecta jerarquías de armaduras/huesos (rigging).
   - Siempre verificar que los assets no utilicen marcas registradas y cumplan con las especificaciones estándar de glTF 2.0 y licencia CC0.

7. **Sin Dependencia de Archivos .env**:
   - La aplicación no requiere ni utiliza variables de entorno en archivos `.env` o `.env.example`.

8. **Ubicación de Documentación y Mensajes de Commit**:
   - Todos los archivos `.md` (`README.md`, `ROADMAP.md`, `ESTRUCTURE.md`, `AI_CONTEXT.md`, `AGENTS.md`) deben residir exclusivamente en la raíz del repositorio.
   - Si existe el archivo `commit_message.txt`, debe estar redactado íntegramente en español y solo actualizarse cuando el usuario lo solicite explícitamente.
