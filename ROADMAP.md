# Roadmap de Desarrollo: Sanatorio 3D

Este documento define la ruta de evolución técnica, narrativa y gráfica del proyecto Sanatorio 3D para Android.

---

## Fase 1: Cimientos y Arquitectura Políglota (Completada)
- [x] Motor de proyección 3D DDA en primera persona para pantalla horizontal.
- [x] Migración del motor de renderizado a **OpenGL ES 3.2** en hilo dedicado (`GLThread`) a 60 FPS estables con `SanatorioGLSurfaceView` y `SanatorioGLRenderer`, eliminando el lag en Compose.
- [x] Texturas PBR de suelo real (`dirty_tiles` / `interior_tiles`) integradas desde assets de Dominio Público (CC0).
- [x] Nueva linterna **Vintage Flashlight** de los años 80 con caja de batería roja desgastada, cono reflector de aluminio pulido y lente de filamento de tungsteno.
- [x] Iluminación lógica de linterna: cono direccional enfocado (*spotlight* con halo de penumbra), atenuación cuadrática inversa y animación de encendido por calentamiento de filamento (`filamentWarmup`).
- [x] Simulación de tubos fluorescentes con parpadeo procedural a 60/120 Hz y cortes de suministro eléctrico.
- [x] Síntesis de audio analógico de terror (zumbido a 60 Hz, pasos sobre azulejos mojados y tono subgrave) en `AudioTrack`.
- [x] Integración de **C++ NDK (CMake)** con enlace de librerías nativas de GPU `GLESv3` y `EGL`.
- [x] Integración de **Lua 5.4.7 (C original de PUC-Rio)** para eventos narrativos y evaluación de estados.
- [x] Integración de **Rust 1.98 (`#[no_std]`)** para lógica de cordura matemática y distancias euclidianas.
- [x] Cuaderno del investigador con plano en tiempo real, expedientes médicos y pestaña de diagnóstico del motor nativo.
- [x] Asset 3D de manos del detective (`investigator_hands`) con rig de 8 articulaciones y guantes de cuero de investigación.
- [x] Componente en primera persona (`FirstPersonHandView`) sincronizado con balanceo al caminar (`headBob`), deslizamiento del interruptor y haz volumétrico en niebla.
- [x] Pestaña interactiva de "Manos 3D & Rig" en el Cuaderno con controles deslizantes para los 8 huesos.
- [x] Modelo 3D de utilería de sanatorio (`wheelchair_01`) con rig de 7 huesos y animaciones de rodado y crujido.
- [x] Flujos de GitHub Actions automatizados: compilación limpia de APK (`build-debug-apk.yml`) y sincronización de mensajes de commit (`override_commit_message.yml`).

---

## Fase 2: Expansión de Puzzles, Carga de Modelos 3D y Guión en Lua
- [ ] Integrar cargador nativo glTF / OBJ (`tinygltf` o `cgltf` en C++) para renderizado de mallas poligonales en escena.
- [ ] Implementar un cargador de scripts Lua desde la carpeta `assets/scripts/` del APK.
- [ ] Puzzles de cajas de fusibles eléctricos: restaurar la energía en el Pabellón Quirúrgico resolviendo circuitos en Lua.
- [ ] Sistema de cerraduras y llaves maestras para abrir salas de tratamiento electroconvulsivo.
- [ ] Diálogos y notas de audio dejadas en grabadoras de cinta de carrete.

---

## Fase 3: Renderizado Avanzado y Sombreado
- [x] Pipeline de shaders OpenGL ES 3.2 con aberración cromática ante baja cordura y corrección de contraste analógico.
- [ ] Sombreado volumétrico y mapas de sombras suaves por columna con aceleración SIMD (ARM Neon) en C++.
- [ ] Partículas de polvo dinámicas y niebla baja flotando bajo los tubos fluorescentes.
- [ ] Animaciones de apertura para puertas de hierro y mirillas de seguridad.

---

## Fase 4: Entidades y Apariciones Hostiles
- [ ] Sistema de inteligencia artificial para sombras errantes en Rust con máquinas de estados finitos.
- [ ] Lógica de huida y escondite en armarios metálicos o celdas vacías.
- [ ] Distorsión auditiva y visual extrema cuando la cordura cae por debajo del 15%.
- [ ] Modos de dificultad: "Investigador Forense" (exploración) y "Pesadilla Clínica" (cordura implacable).

---

## Fase 5: Distribución y Optimización Móvil
- [ ] Configuración optimizada de empaquetado para tiendas de terceros (Uptodown / descarga directa de APK).
- [ ] Soporte para mandos físicos Bluetooth (gamepads) en Android.
- [ ] Modo de alta tasa de refresco (90 Hz / 120 Hz) en pantallas AMOLED.
