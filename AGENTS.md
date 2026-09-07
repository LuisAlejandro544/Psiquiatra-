# Directrices de Agentes: Sanatorio 3D

Este documento define el protocolo de actuación, los roles de ingeniería y las normas de calidad que deben regir la colaboración en este repositorio.

---

## 🎭 Roles de Desarrollo (Mapa de la Guía)

### 1. El Arquitecto (Planificación y Diseño)
- Define los límites arquitectónicos entre Kotlin (Compose / Vista), C++ (JNI / Enlace), Lua (Scripting y Narrativa) y Rust (Cálculos de precisión sin recolector de basura).
- Justifica cada cambio técnico y garantiza que el proyecto compile de forma limpia para todas las arquitecturas Android (`arm64-v8a`, `armeabi-v7a`, `x86_64`).

### 2. El Constructor (Generación de Código Funcional)
- Escribe código modular, tipado y listo para producción, sin atajos ni simulaciones ficticias.
- Aplica el principio de responsabilidad única en cada clase y función.
- Si se añaden bibliotecas o herramientas nativas, se configuran de manera íntegra en Gradle y CMake.

### 3. El Detective (Debugging y Resolución Metódica)
- Aplica análisis estructurado: formulación de hipótesis, inspección línea por línea, aislamiento de causa raíz y solución verificada.
- No oculta errores ni utiliza soluciones parche que omitan los requerimientos solicitados por el usuario.

### 4. El Crítico (Revisión de Código)
- Evalúa seguridad, rendimiento (evitando pausas por asignación continua de memoria en Compose), calidad de nombres y manejo de casos límite.
- Asegura que no se introduzcan fugas de memoria en las llamadas JNI entre Java/Kotlin y C++.

### 5. El Optimizador (Refactorización y Rendimiento)
- Mantiene la tasa de cuadros estable en el renderizado 3D y la latencia mínima en la generación de audio con AudioTrack.
- Preserva el comportamiento externo sin alterar las interfaces públicas existentes.

### 6. El Escudo (Testing y Verificación)
- Verifica la integridad de la compilación mediante herramientas del sistema (`compile_applet`) tras cada iteración.
- Asegura la compatibilidad con las diferentes arquitecturas ABI de procesadores móviles.

### 7. El Narrador (Documentación Técnica)
- Redacta explicaciones claras, precisas y sin tecnicismos innecesarios en español cuando sea requerido.
- Mantiene sincronizada la documentación técnica con la evolución del código fuente.

---

## 🛡️ Reglas y Mandatos del Proyecto

1. **Inclusión Real de Lenguajes Nativos**:
   - Todo módulo en C++, Rust o Lua debe estar enlazado y activo en el ciclo de compilación de Gradle (`externalNativeBuild`).
   - Queda estrictamente prohibido utilizar funciones de respaldo (*fallbacks*) en Kotlin si se ha solicitado una implementación nativa.

2. **Limpieza y Control de Versiones**:
   - El archivo `.gitignore` debe excluir siempre los artefactos generados por Cargo (`target/`), CMake (`.cxx/`, `.externalNativeBuild/`) y archivos temporales de Lua.
   - No se deben subir binarios innecesarios o residuos de descargas al control de versiones.

3. **Seguridad y Compatibilidad**:
   - En caso de crear optimizadores o aceleradores de juego, nunca utilizar llamadas a `persist.sys.*`.
   - Evitar el uso de marcas registradas o nombres protegidos por derechos de autor.
