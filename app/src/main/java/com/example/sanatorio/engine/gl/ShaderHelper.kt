package com.example.sanatorio.engine.gl

import android.opengl.GLES32
import android.util.Log

/**
 * Utilidades para compilación de shaders y enlace de programas OpenGL ES 3.2.
 */
object ShaderHelper {
    private const val TAG = "SanatorioGLShader"

    fun compileShader(shaderType: Int, shaderSource: String): Int {
        val shaderId = GLES32.glCreateShader(shaderType)
        if (shaderId == 0) {
            Log.e(TAG, "No se pudo crear shader de tipo: $shaderType")
            return 0
        }

        GLES32.glShaderSource(shaderId, shaderSource)
        GLES32.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES32.glGetShaderiv(shaderId, GLES32.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES32.glGetShaderInfoLog(shaderId)
            Log.e(TAG, "Error compilando shader ($shaderType):\n$log\nSource:\n$shaderSource")
            GLES32.glDeleteShader(shaderId)
            return 0
        }

        return shaderId
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = GLES32.glCreateProgram()
        if (programId == 0) {
            Log.e(TAG, "No se pudo crear el programa GL")
            return 0
        }

        GLES32.glAttachShader(programId, vertexShaderId)
        GLES32.glAttachShader(programId, fragmentShaderId)
        GLES32.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES32.glGetProgramiv(programId, GLES32.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES32.glGetProgramInfoLog(programId)
            Log.e(TAG, "Error enlazando programa GL:\n$log")
            GLES32.glDeleteProgram(programId)
            return 0
        }

        return programId
    }

    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShaderId = compileShader(GLES32.GL_VERTEX_SHADER, vertexSource)
        val fragmentShaderId = compileShader(GLES32.GL_FRAGMENT_SHADER, fragmentSource)

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0
        }

        return linkProgram(vertexShaderId, fragmentShaderId)
    }
}
