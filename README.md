# Sanatorio 3D - Juego de Terror en Primera Persona para Android

Sanatorio 3D es una experiencia de terror y misterio psicológico en primera persona desarrollada para dispositivos móviles Android en orientación horizontal (*landscape*).

El jugador asume el papel de un detective privado que se adentra en el abandonado **Sanatorio Psiquiátrico San Rafael** en 1984 para recuperar el historial médico y las evidencias clínicas del Dr. Valdés tras una serie de incidentes no resueltos en el Pabellón C.

---

## 🏛️ Arquitectura Políglota del Motor

La aplicación combina Kotlin + Jetpack Compose con un subsistema nativo de alto rendimiento:

1. **Kotlin & Jetpack Compose**: Interfaz táctil de baja latencia, HUD cinematográfico, sombreado de viñeta radial y cuaderno interactivo del caso.
2. **C++ (NDK 28 / Clang 17)**: Conexión JNI (`NativeEngineBridge`), control manual de memoria sin pausas de Garbage Collector e integración del runtime.
3. **Lua 5.4.7 (C Puro oficial)**: El motor de scripting original en C de la PUC-Rio, sin envoltorios de terceros. Permite orquestar eventos narrativos, evaluación de cordura ante pacientes y respuestas dinámicas del sanatorio.
4. **Rust 1.98 (`asylum_core_rust`)**: Núcleo estático libre de dependencias pesadas (`#[no_std]`), responsable del cálculo preciso del decaimiento de cordura y del cálculo de distancias para detección de tensión y sustos.

---

## 🎮 Mecánicas de Juego

- **Vista en Primera Persona (Horizontal)**: Entorno 3D con textura de azulejos agrietados, celdas de aislamiento con acolchado de cuero, rejas carcelarias oxidadas y puertas de alta seguridad con mirilla.
- **Linterna con Haz Dinámico**: Atenuación de luz, interruptor sonoro táctil y cálculo del impacto en la estabilidad psicológica del protagonista.
- **Luces Fluorescentes Inestables**: Parpadeos procedurales a 60/120 Hz, caídas de tensión y apagones totales inesperados.
- **Audio Procedural de Tensión**: Zumbido eléctrico, pisadas sobre baldosas mojadas, chasquido de interruptores y frecuencias graves continuas generadas sintéticamente.
- **Cuaderno del Investigador**: Consulta de expedientes clínicos archivados, plano esquemático del sanatorio en tiempo real y panel de diagnóstico del subsistema nativo.
- **Modelos 3D Integrados (.gltf / .obj / .mtl)**:
  - **Linterna Industrial de Investigador**: Modelos 3D completos con tubos cilíndricos, anillos de agarre de goma antideslizante, reflector parabólico cromado, interruptor deslizante mecánico y bombilla incandescente.
  - **Estados de Iluminación Reales**:
    - **Con Luces (Encendida)**: `flashlight_lights_on.obj` / `flashlight_lights_on.mtl` con material emisivo cálido (`Ke 1.0 0.88 0.45`), lente de cristal templado traslúcido y cono volumétrico de proyección.
    - **Sin Luces (Apagada)**: `flashlight_lights_off.obj` / `flashlight_lights_off.mtl` con filamento apagado (`Ke 0.0`), interruptor retraído y ausencia de haz de luz.
    - **glTF 2.0 Animado**: `flashlight.gltf` autocontenido con materiales PBR metálico-rugosidad, jerarquía de nodos articulados y animación de deslizamiento del interruptor.

---

## 🛠️ Requisitos y Compilación

- **Android SDK**: `minSdk 24`, `targetSdk 36`, `compileSdk 36`
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

El repositorio incluye el flujo de trabajo automatizado `.github/workflows/build-debug-apk.yml`:
1. **Descarga el código fuente**: Clona el repositorio completo.
2. **Prepara dependencias C++, Rust y Lua**: Instala CMake, Ninja, el compilador de Rust con los 3 targets Android (`aarch64`, `armv7`, `x86_64`) y compila las librerías nativas.
3. **Firma Automática Obligatoria**: Ejecuta `./ensure_keystore.sh` para generar de manera desatendida la firma `debug.keystore`, eliminando esperas interactivas o bloqueos por credenciales faltantes.
4. **Compilación Limpia Sin Caché**: Ejecuta `gradle assembleDebug --no-build-cache --no-daemon` para garantizar una compilación fresca y reproducible.
5. **Artefacto Disponible**: Publica `app-debug.apk` como artefacto descargable en la pestaña *Actions* de GitHub.

---

## ⚙️ Scripts de Automatización

- **`./ensure_keystore.sh`**: Verifica la existencia de `debug.keystore`. Si no existe, la crea al instante mediante `keytool` para garantizar que la compilación Debug esté siempre firmada y lista.
- **`./organize_md_to_root.sh`**: Centraliza todos los archivos de documentación Markdown (`.md`) exclusivamente en la raíz del repositorio, evitando duplicados en carpetas de módulos.
