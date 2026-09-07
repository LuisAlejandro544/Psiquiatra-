# Sanatorio 3D - Juego de Terror en Primera Persona para Android

Sanatorio 3D es una experiencia de terror y misterio psicológico en primera persona desarrollada para dispositivos móviles Android en orientación horizontal (*landscape*).

El jugador asume el papel de un detective privado que se adentra en el abandonado **Sanatorio Psiquiátrico San Rafael** en 1984 para recuperar el historial médico y las evidencias clínicas del Dr. Valdés tras una serie de incidentes no resueltos en el Pabellón C.

---

## 🏛️ Arquitectura Políglota del Motor

La aplicación combina Kotlin + Jetpack Compose con un subsistema nativo de alto rendimiento acelerado por GPU:

1. **OpenGL ES 3.2 (Pipeline de GPU en GLThread)**: Renderizado tridimensional a 60 FPS estables ejecutado en un hilo de GPU independiente (`SanatorioGLSurfaceView` + `SanatorioGLRenderer`). Elimina por completo el lag y las pausas de recolección de basura ocasionadas por recomposiciones continuas en Compose. Incorpora shaders GLSL 3.0/3.2 con distorsión por baja cordura, atenuación física de luz y viñeteado claustrofóbico.
2. **Kotlin & Jetpack Compose**: Interfaz táctil de latencia mínima, controles táctiles analógicos, HUD cinematográfico, sombreado de atmósfera y cuaderno interactivo del caso con notas forenses.
3. **C++ (NDK 28 / Clang 17)**: Conexión JNI (`NativeEngineBridge`), enlace nativo de `GLESv3` y `EGL`, control manual de memoria y telemetría de rendimiento del hardware.
4. **Lua 5.4.7 (C Puro oficial)**: El motor de scripting original en C de la PUC-Rio, sin envoltorios de terceros. Permite orquestar eventos narrativos, evaluación de cordura ante pacientes y respuestas dinámicas del sanatorio.
5. **Rust 1.98 (`asylum_core_rust`)**: Núcleo estático libre de dependencias pesadas (`#[no_std]`), responsable del cálculo preciso del decaimiento de cordura y del cálculo de distancias euclidianas para detección de tensión y sustos.

---

## 🎮 Mecánicas de Juego

- **Vista en Primera Persona Hardware-Accelerated (OpenGL ES 3.2)**: Entorno tridimensional renderizado por GPU con textura real de baldosas desgastadas del sanatorio (`dirty_tiles` / `interior_tiles`), celdas de aislamiento con acolchado de cuero, rejas carcelarias oxidadas y puertas de alta seguridad con mirilla.
- **Nueva Linterna Vintage (`vintage_flashlight`)**:
  - Modelo industrial de 1984 con caja de batería roja desgastada, remaches de latón y asa de baquelita negra.
  - Cono reflector pulido de aluminio y lente estriada con bulbo de tungsteno visible.
  - Empuñadura ergonómica sostenida firmemente por el guante de investigación forense (`investigator_hands`).
  - **Efecto de Encendido Realista**: Animación de incandescencia gradual del filamento (`filamentWarmup`), interruptor deslizante superior accionado físicamente por el pulgar, y haz de luz cónico volumétrico con partículas de polvo en suspensión.
  - **Iluminación Lógica**: Atenuación cuadrática física, haz central enfocado (*spotlight* a 3200K de luz cálida) y caída angular suave hacia la penumbra.
- **Luces Fluorescentes Inestables**: Parpadeos procedurales a 60/120 Hz, caídas de tensión y apagones totales inesperados sincronizados con zumbido eléctrico en audio sintético.
- **Audio Procedural de Tensión**: Zumbido a 60 Hz con armónicos, pisadas sobre azulejos mojados, chasquido mecánico de interruptor y frecuencias graves continuas generadas sintéticamente mediante `AudioTrack`.
- **Cuaderno del Investigador**: Consulta de expedientes clínicos archivados, plano esquemático del sanatorio en tiempo real y panel de diagnóstico del subsistema nativo (C++, Rust, Lua, OpenGL ES 3.2).
- **Modelos y Texturas PBR Integradas (CC0)**:
  - **Texturas del Suelo (`dirty_tiles` / `interior_tiles`)**: Mapas PBR (difuso, normales, rugosidad, ARM y oclusión) extraídos directamente de Poly Haven con licencia de Dominio Público.
  - **Manos Articuladas del Investigador (`investigator_hands`)**: Malla 3D completa (3,975 vértices) con guantes de cuero de investigación forense, armadura esquelética modular de 8 articulaciones, animaciones de agarre, temblor y extensión.
  - **Silla de Ruedas del Sanatorio (`wheelchair_01`)**: Modelo 3D de hospital psiquiátrico con 7 huesos articulados con animaciones de crujido y rodado.

---

## 🛠️ Requisitos y Compilación

- **Android SDK**: `minSdk 24`, `targetSdk 36`, `compileSdk 36`
- **GPU Feature**: OpenGL ES 3.2 requerida (`android.hardware.opengles.version 0x00030002`)
- **NDK**: Versión 26+ / 28+ con soporte para `arm64-v8a`, `armeabi-v7a` y `x86_64`
- **CMake**: Versión 3.22.1 o superior
- **Rust Toolchain**: `rustc 1.98+` con targets `aarch64-linux-android`, `armv7-linux-androideabi` y `x86_64-linux-android`
- **Variables de Entorno**: No se requiere ningún archivo `.env` para compilar el proyecto.
- **Compilar APK Local**:
  ```bash
  ./ensure_keystore.sh
  gradle assembleDebug
  ```

---

## 🚀 Integración Continua (GitHub Actions)

El repositorio incluye flujos de trabajo automatizados en `.github/workflows/`:
1. **`build-debug-apk.yml`**:
   - Prepara dependencias C++, Rust (con sus 3 targets Android), Lua y OpenGL ES.
   - Ejecuta `./ensure_keystore.sh` para garantizar la firma `debug.keystore`.
   - Compila el APK Debug (`gradle assembleDebug --no-build-cache --no-daemon`) y publica el artefacto.
2. **`override_commit_message.yml`**:
   - Inspecciona `commit_message.txt` tras cada push y sincroniza el historial git automáticamente.

---

## ⚙️ Scripts de Automatización

- **`./ensure_keystore.sh`**: Verifica la existencia de `debug.keystore` o la crea automáticamente con `keytool`.
- **`./organize_md_to_root.sh`**: Centraliza todos los archivos de documentación Markdown (`.md`) exclusivamente en la raíz del repositorio.
- **`./fetch_assets.sh`**: Busca, descarga y desempaqueta modelos 3D y texturas con licencia libre CC0 (Dominio Público) e inyecta armaduras de huesos (rigging) y animaciones editables.
