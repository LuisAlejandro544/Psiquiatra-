package com.example.sanatorio.nativebridge

object NativeEngineBridge {

    init {
        try {
            System.loadLibrary("sanatorio_native")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun initNativeEngine()

    external fun executeLuaScript(script: String): String

    external fun getEngineStatus(): String

    external fun calculateSanityRust(
        currentSanity: Float,
        flashlightOn: Boolean,
        flickerLevel: Float,
        deltaTimeSec: Float
    ): Float

    external fun calculateDistanceRust(
        px: Float,
        py: Float,
        tx: Float,
        ty: Float
    ): Float
}
