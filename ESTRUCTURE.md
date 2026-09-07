# Estructura del Proyecto: Sanatorio 3D

Este documento describe la organización jerárquica de carpetas, módulos de código fuente y distribución de responsabilidades en la arquitectura políglota de la aplicación.

---

## Árbol General de Directorios

```text
/ (Raíz del Repositorio)
├── .github/workflows/
│   ├── build-debug-apk.yml             # Workflow de CI/CD para compilar APK Debug (C++, Rust, Lua, sin cache)
│   └── override_commit_message.yml     # Workflow para sincronizar mensajes de commit desde commit_message.txt
├── .gitignore                          # Reglas de exclusión para C++, Rust, Lua y artefactos de compilación
├── build.gradle.kts                    # Configuración de Gradle a nivel de raíz
├── settings.gradle.kts                 # Configuración de proyectos y repositorios incluidos
├── commit_message.txt                  # Mensaje descriptivo para commits automáticos sincronizados por CI
├── ensure_keystore.sh                  # Script que garantiza y genera la firma debug.keystore sin esperas
├── organize_md_to_root.sh              # Script para centralizar los archivos .md exclusivamente en la raíz
├── fetch_assets.sh                     # Script para descargar y procesar modelos 3D y texturas CC0 con rig
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
        ├── AndroidManifest.xml         # Manifiesto de la aplicación (orientación horizontal fijada)
        │
        ├── assets                      # Recursos 3D y multimedia empaquetados en el APK
        │   └── models/
        │       ├── flashlight/         # Modelos 3D de la linterna con materiales, luces y estados
        │       │   ├── flashlight.gltf           # Modelo glTF 2.0 (PBR, jerarquía y animación de interruptor)
        │       │   ├── flashlight_lights_on.obj  # Malla 3D en modo ENCENDIDO (foco emisivo y haz cónico)
        │       │   ├── flashlight_lights_on.mtl  # Materiales PBR y propiedades de emisión de luz
        │       │   ├── flashlight_lights_off.obj # Malla 3D en modo APAGADO (filamento oscuro y botón retraído)
        │       │   └── flashlight_lights_off.mtl # Materiales en reposo sin emisión lumínica
        │       ├── investigator_hands/ # Asset 3D de manos del detective (CC0, rig de 8 articulaciones)
        │       │   ├── investigator_hands.gltf   # Malla 3D 3,975 vértices, esqueleto y animación
        │       │   ├── investigator_hands.bin    # Buffers geométricos de piel y huesos
        │       │   ├── investigator_hands.obj    # Malla poligonal estándar de respaldo
        │       │   ├── investigator_hands.mtl    # Definición de materiales
        │       │   ├── textures/                 # Mapas de textura PBR (Albedo, Normales, Roughness/Metallic)
        │       │   ├── rig_info.json             # Especificación técnica de las 8 articulaciones
        │       │   └── LICENSE_CC0.txt           # Garantía legal de Dominio Público
        │       └── wheelchair_01/      # Utilería de hospital psiquiátrico con rig de 7 huesos
        │           ├── wheelchair_01.gltf        # Modelo glTF con jerarquía de ruedas y respaldo
        │           └── rig_info.json             # Mapeo de articulaciones y animación de rodado
        │
        ├── cpp                         # Capa C++ nativa y motor Lua
        │   ├── CMakeLists.txt          # Script de compilación CMake que orquesta C++, Rust y Lua
        │   ├── native_bridge.cpp       # Conexión JNI entre Kotlin y los motores C/Rust
        │   ├── rust_bridge.h           # Cabecera C de las funciones exportadas por Rust
        │   └── lua/                    # Fuentes oficiales de Lua 5.4.7 en C puro (sin wrappers)
        │       ├── lua.h               # Cabecera principal de Lua
        │       ├── lauxlib.h           # Librería auxiliar de Lua
        │       ├── lualib.h            # Librerías estándar de Lua
        │       ├── lapi.c              # API de Lua C
        │       ├── lvm.c               # Máquina virtual de Lua
        │       └── ... (32 archivos fuente .c y cabeceras .h de PUC-Rio)
        │
        ├── rust                        # Módulo nativo en Rust (asylum_core_rust)
        │   ├── Cargo.toml              # Configuración de crate con staticlib y panic="abort"
        │   └── src
        │       └── lib.rs              # Cálculos de cordura y distancias en #[no_std]
        │
        ├── java/com/example/sanatorio
        │   ├── MainActivity.kt         # Actividad principal, bloqueo en horizontal e inmersión
        │   │
        │   ├── engine                  # Motores de renderizado y audio procedural
        │   │   ├── RaycastRenderer.kt            # Motor 3D DDA con sombreado de linterna y texturas
        │   │   └── HorrorAudioSynthesizer.kt      # Generador de audio procedural analógico
        │   │
        │   ├── model                   # Modelos de datos del sanatorio
        │   │   ├── AsylumMap.kt                  # Plano de 24x24 casillas del centro psiquiátrico
        │   │   ├── Decoration.kt                 # Objetos decorativos (sillas de ruedas, camillas, goteros)
        │   │   ├── GameState.kt                  # Estado del jugador (posición, linterna, cordura, rig)
        │   │   └── HandRigModel.kt               # Definición reactiva de los 8 huesos, ángulos y presets
        │   │
        │   ├── nativebridge            # Puente JNI desde Kotlin
        │   │   └── NativeEngineBridge.kt         # Carga de libsanatorio_native.so y llamadas nativas
        │   │
        │   └── ui                      # Interfaz de usuario en Jetpack Compose
        │       ├── SanatorioGameScreen.kt        # Pantalla del juego, joystick táctil y visor 3D
        │       ├── FirstPersonHandView.kt        # Vista en primera persona de manos, linterna y temblor
        │       ├── InvestigatorNotebookDialog.kt # Cuaderno con evidencias, mapa, motor y pestaña de rig 3D
        │       └── theme
        │           ├── Color.kt                  # Paleta de colores fríos y oscuros de terror
        │           ├── Theme.kt                  # Tema Material Design 3 de Sanatorio
        │           └── Type.kt                   # Tipografía de la interfaz
        │
        └── res                         # Recursos Android
            ├── values/strings.xml       # Nombres y textos de la app
            ├── drawable/                # Iconos y formas gráficas vectoriales
            └── mipmap-*/                # Icono del lanzador de la app
```

---

## Responsabilidad de Cada Módulo

| Módulo | Lenguaje | Propósito Principal |
| :--- | :--- | :--- |
| **`SanatorioGameScreen`** | Kotlin / Compose | Bucle principal a 30-60 FPS, entrada táctil dual, interfaz HUD cinematográfica y renderizado de Canvas. |
| **`FirstPersonHandView`** | Kotlin / Compose | Perspectiva subjetiva (FPS) de manos y linterna con inercia de paso (`headBob`), pulsación de interruptor y espasmo por pánico. |
| **`InvestigatorNotebookDialog`** | Kotlin / Compose | Cuaderno de investigación con pestañas de expediente, mapa en vivo, telemetría nativa y control individual de 8 huesos con sliders 3D. |
| **`HandRigModel`** | Kotlin | Gestor de estado para las 8 articulaciones del modelo 3D, presets de animación y sincronización con archivos glTF. |
| **`RaycastRenderer`** | Kotlin | Cálculo DDA columna a columna, texturas procedurales (azulejos agrietados, celdas, rejas) y atenuación de luz por linterna y parpadeos. |
| **`HorrorAudioSynthesizer`** | Kotlin / AudioTrack | Generación en tiempo real de tonos continuos subgraves, zumbido de reactancia fluorescente y chasquidos mecánicos. |
| **`native_bridge.cpp`** | C++17 | Gestión del ciclo de vida del intérprete Lua, enlace con la librería estática de Rust y exposición JNI a la JVM. |
| **`lua/`** | C (Lua 5.4.7) | Evaluación de scripts de eventos de terror, cálculo de impacto por encuentros y lógica de narrativa procedural. |
| **`asylum_core_rust`** | Rust (`#[no_std]`) | Motor matemático de decaimiento de cordura y cálculo vectorial de distancias sin recolector de basura. |
