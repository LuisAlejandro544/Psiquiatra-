# Estructura del Proyecto: Sanatorio 3D

Este documento describe la organización jerárquica de carpetas, módulos de código fuente y distribución de responsabilidades en la arquitectura políglota de la aplicación.

---

## Árbol General de Directorios

```text
/ (Raíz del Repositorio)
├── .github/workflows/
│   ├── build-debug-apk.yml             # Workflow de CI/CD para compilar APK Debug (C++, Rust, Lua, OpenGL ES)
│   └── override_commit_message.yml     # Workflow para sincronizar mensajes de commit desde commit_message.txt
├── .gitignore                          # Reglas de exclusión para C++, Rust, Lua y artefactos de compilación
├── build.gradle.kts                    # Configuración de Gradle a nivel de raíz
├── settings.gradle.kts                 # Configuración de proyectos y repositorios incluidos
├── commit_message.txt                  # Mensaje descriptivo para commits automáticos sincronizados por CI
├── ensure_keystore.sh                  # Script que garantiza y genera la firma debug.keystore sin esperas
├── organize_md_to_root.sh              # Script para centralizar los archivos .md exclusivamente en la raíz
├── fetch_assets.sh                     # Script para descargar y procesar modelos 3D y texturas CC0 con PBR y rig
├── README.md                           # Resumen del proyecto, características y modo de compilación
├── ROADMAP.md                          # Plan de desarrollo y etapas futuras
├── ESTRUCTURE.md                       # Estructura detallada del proyecto y árbol de archivos
├── AI_CONTEXT.md                       # Contexto técnico y directivas para asistentes de IA
├── AGENTS.md                           # Protocolos de trabajo, directrices de arquitectura y buenas prácticas
│
└── app/
    ├── build.gradle.kts                # Configuración del módulo Android, NDK (CMake) y Compose (sin .env)
    └── src/
        └── main/
        ├── AndroidManifest.xml         # Manifiesto con OpenGL ES 3.2 requerido y orientación horizontal
        │
        ├── assets                      # Recursos 3D y multimedia empaquetados en el APK
        │   ├── textures/
        │   │   ├── dirty_tiles/        # Texturas PBR de azulejos sucios del hospital (Poly Haven CC0)
        │   │   │   ├── dirty_tiles_diffuse.jpg
        │   │   │   ├── dirty_tiles_normal.jpg
        │   │   │   ├── dirty_tiles_roughness.jpg
        │   │   │   └── dirty_tiles_arm.jpg
        │   │   └── interior_tiles/     # Texturas PBR de azulejos interiores desgastados
        │   │       ├── interior_tiles_diffuse.jpg
        │   │       ├── interior_tiles_normal.jpg
        │   │       └── interior_tiles_roughness.jpg
        │   └── models/
        │       ├── flashlight/         # Modelo 3D de la linterna vintage de época
        │       │   ├── flashlight.gltf           # Modelo glTF 2.0 con jerarquía de nodos articulados
        │       │   ├── textures/                 # Texturas PBR de la linterna vintage (aluminio y pintura roja)
        │       │   │   ├── vintage_flashlight_diff_1k.jpg
        │       │   │   ├── vintage_flashlight_nor_gl_1k.jpg
        │       │   │   └── vintage_flashlight_rough_1k.jpg
        │       │   └── rig_info.json             # Jerarquía de huesos para animación de interruptor y empuñadura
        │       ├── investigator_hands/ # Asset 3D de manos del detective (CC0, rig de 8 articulaciones)
        │       │   ├── investigator_hands.gltf   # Malla 3D 3,975 vértices, esqueleto y animación
        │       │   ├── investigator_hands.bin    # Buffers geométricos de piel y huesos
        │       │   ├── textures/                 # Mapas de textura PBR de guantes de investigación
        │       │   ├── rig_info.json             # Especificación técnica de las 8 articulaciones
        │       │   └── LICENSE_CC0.txt           # Garantía legal de Dominio Público
        │       └── wheelchair_01/      # Utilería de hospital psiquiátrico con rig de 7 huesos
        │           ├── wheelchair_01.gltf        # Modelo glTF con jerarquía de ruedas y respaldo
        │           └── rig_info.json             # Mapeo de articulaciones y animación de rodado
        │
        ├── cpp                         # Capa C++ nativa, EGL/GLESv3 y motor Lua
        │   ├── CMakeLists.txt          # Script CMake que orquesta C++, Rust, Lua, GLESv3 y EGL
        │   ├── native_bridge.cpp       # Conexión JNI, soporte de telemetría OpenGL ES 3.2 y Lua/Rust
        │   ├── rust_bridge.h           # Cabecera C de las funciones exportadas por Rust
        │   └── lua/                    # Fuentes oficiales de Lua 5.4.7 en C puro (sin wrappers)
        │       ├── lua.h               # Cabecera principal de Lua
        │       ├── lauxlib.h           # Librería auxiliar de Lua
        │       └── lualib.h            # Librerías estándar de Lua
        │
        ├── rust/                       # Subproyecto Rust (asylum_core_rust)
        │   ├── Cargo.toml              # Configuración de compilación #[no_std] y staticlib
        │   └── src/
        │       └── lib.rs              # Cálculo estático de cordura y distancias de terror
        │
        └── java/com/example/
            ├── MainActivity.kt         # Actividad principal con flags inmersivos
            ├── sanatorio/
            │   ├── engine/
            │   │   ├── gl/
            │   │   │   ├── SanatorioGLSurfaceView.kt # Superficie de renderizado continuo en GLThread a 60 FPS
            │   │   │   ├── SanatorioGLRenderer.kt    # Renderizador OpenGL ES 3.2 con shaders de hardware y luz cónica
            │   │   │   ├── ShaderHelper.kt           # Compilación de shaders y programas GLSL 3.0/3.2
            │   │   │   └── TextureLoader.kt          # Cargador de texturas PBR y bitmaps de GPU
            │   │   ├── HorrorAudioSynthesizer.kt     # Generación de audio procedural a 60 Hz
            │   │   ├── RaycastRenderer.kt            # Renderizador de reserva y utilidades
            │   │   └── TextureGenerator.kt           # Generación procedural de texturas de paredes y sprites
            │   ├── model/
            │   │   ├── AsylumMap.kt                  # Matriz 20x20 del hospital y posiciones de utilería
            │   │   ├── Decoration.kt                 # Entidades y objetos inspeccionables
            │   │   ├── GameState.kt                  # Estado reactivo del jugador y linterna
            │   │   └── HandRigModel.kt               # Modelo de datos de los 8 huesos de la mano 3D
            │   ├── nativebridge/
            │   │   └── NativeEngineBridge.kt         # Enlace Kotlin con la librería sanatorio_native
            │   └── ui/
            │       ├── FirstPersonHandView.kt        # Vista en primera persona de la linterna vintage y manos
            │       ├── NotebookDialog.kt             # Cuaderno del caso y panel de telemetría
            │       └── SanatorioGameScreen.kt        # Pantalla principal con AndroidView(GLSurfaceView) y HUD
            └── ui/theme/
                ├── Color.kt                          # Paleta de colores de terror psicológico
                └── Theme.kt                          # Tema Material 3 oscuro
```
