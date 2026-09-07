# Roadmap de Desarrollo: Sanatorio 3D

Este documento define la ruta de evolución técnica, narrativa y gráfica del proyecto Sanatorio 3D para Android.

---

## Fase 1: Cimientos y Arquitectura Políglota (Completada)
- [x] Motor de proyección 3D DDA en primera persona para pantalla horizontal.
- [x] Texturizado procedural con estética de sanatorio decadente de los años 80.
- [x] Haz de linterna dinámico con interruptor de botón táctil y sonido mecánico.
- [x] Simulación de tubos fluorescentes con parpadeo y cortes de suministro eléctrico.
- [x] Síntesis de audio analógico de terror (zumbido a 60/120 Hz, pasos y tono subgrave).
- [x] Integración de **C++ NDK (CMake)** para eliminación de pausas de Garbage Collector.
- [x] Integración de **Lua 5.4.7 (C original de PUC-Rio)** para eventos narrativos y evaluación de estados.
- [x] Integración de **Rust 1.98 (`#[no_std]`)** para lógica de cordura matemática y distancias seguras.
- [x] Cuaderno del investigador con plano en tiempo real, expedientes médicos y pestaña del motor nativo.
- [x] Modelos 3D de linterna creados y configurados (`.gltf` 2.0 y `.obj` / `.mtl`) con doble estado: encendida con emisión y apagada, con jerarquía de nodos y animación de interruptor.

---

## Fase 2: Expansión de Puzzles, Carga de Modelos 3D y Guión en Lua
- [ ] Integrar cargador nativo glTF / OBJ (`tinygltf` o `cgltf` en C++) para renderizado de mallas poligonales en escena.
- [ ] Implementar un cargador de scripts Lua desde la carpeta `assets/scripts/` del APK.
- [ ] Puzzles de cajas de fusibles eléctricos: restaurar la energía en el Pabellón Quirúrgico resolviendo circuitos en Lua.
- [ ] Sistema de cerraduras y llaves maestras para abrir salas de tratamiento electroconvulsivo.
- [ ] Diálogos y notas de audio dejadas en grabadoras de cinta de carrete.

---

## Fase 3: Renderizado Avanzado y Sombreado
- [ ] Sombreado volumétrico y mapas de sombras suaves por columna con aceleración SIMD (ARM Neon) en C++.
- [ ] Partículas de polvo y niebla baja flotando bajo los tubos fluorescentes.
- [ ] Texturas con mapas de rugosidad y manchas de sangre seca con mayor resolución.
- [ ] Animaciones de apertura para puertas de hierro y mirillas de seguridad.

---

## Fase 4: Entidades y Apariciones Hostiles
- [ ] Sistema de inteligencia artificial para sombras errantes en Rust con máquinas de estados finitos.
- [ ] Lógica de huida y escondite en armarios metálicos o celdas vacías.
- [ ] Distorsión auditiva y visual (efecto de aberración cromática) cuando la cordura cae por debajo del 20%.
- [ ] Modos de dificultad: "Investigador Forense" (exploración) y "Pesadilla Clínica" (cordura implacable).

---

## Fase 5: Distribución y Optimización Móvil
- [ ] Configuración optimizada de empaquetado para tiendas de terceros (Uptodown / descarga directa de APK).
- [ ] Soporte para mandos físicos Bluetooth (gamepads) en Android.
- [ ] Modo de alta tasa de refresco (90 Hz / 120 Hz) en pantallas AMOLED.
