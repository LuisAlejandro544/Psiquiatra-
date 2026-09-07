package com.example.sanatorio.engine.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.GameState

/**
 * Vista de superficie dedicada para OpenGL ES 3.2.
 * Ejecuta el renderizado 3D de Sanatorio a 60 FPS en un hilo nativo independiente (GLThread),
 * eliminando el lag causado por recomposiciones continuas en el hilo principal de Compose.
 */
class SanatorioGLSurfaceView @JvmOverloads constructor(
    context: Context,
    gameState: GameState,
    map: AsylumMap,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: SanatorioGLRenderer

    init {
        // Solicitar contexto OpenGL ES 3.2 (ClientVersion 3)
        setEGLContextClientVersion(3)
        // 8 bits por canal con stencil y profundidad
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        renderer = SanatorioGLRenderer(context, gameState, map)
        setRenderer(renderer)

        // Renderizado continuo a 60 FPS gestionado por la GPU
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }
}
