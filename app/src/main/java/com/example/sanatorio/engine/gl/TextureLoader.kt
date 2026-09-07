package com.example.sanatorio.engine.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES32
import android.opengl.GLUtils
import android.util.Log
import com.example.sanatorio.engine.TextureGenerator
import java.io.InputStream

/**
 * Gestor de texturas para OpenGL ES 3.2.
 * Carga texturas PBR reales descargadas con fetch_assets.sh (como dirty_tiles)
 * y genera texturas procedurales para paredes y sprites en la memoria de la GPU.
 */
object TextureLoader {
    private const val TAG = "SanatorioTexLoader"

    var floorTextureId: Int = 0
        private set
    var wallTextureAtlasId: Int = 0
        private set

    // Cache de texturas
    private val textureMap = mutableMapOf<String, Int>()

    /**
     * Carga un bitmap desde los assets del APK o retorna null si falla.
     */
    fun loadBitmapFromAssets(context: Context, assetPath: String, reqWidth: Int = 256, reqHeight: Int = 256): Bitmap? {
        return try {
            val inputStream: InputStream = context.assets.open(assetPath)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val original = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            if (original != null && (original.width != reqWidth || original.height != reqHeight)) {
                Bitmap.createScaledBitmap(original, reqWidth, reqHeight, true)
            } else {
                original
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar asset '$assetPath': ${e.message}")
            null
        }
    }

    /**
     * Carga una textura en OpenGL ES 3.2 a partir de un Bitmap.
     */
    fun loadTextureFromBitmap(bitmap: Bitmap, generateMipmap: Boolean = true): Int {
        val textureHandle = IntArray(1)
        GLES32.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureHandle[0])

            // Filtrado bilineal de alta fidelidad
            GLES32.glTexParameteri(
                GLES32.GL_TEXTURE_2D,
                GLES32.GL_TEXTURE_MIN_FILTER,
                if (generateMipmap) GLES32.GL_LINEAR_MIPMAP_LINEAR else GLES32.GL_LINEAR
            )
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_REPEAT)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_REPEAT)

            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)
            if (generateMipmap) {
                GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D)
            }
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0)
        }
        return textureHandle[0]
    }

    /**
     * Carga una textura desde un array ARGB_8888 de píxeles en OpenGL ES 3.2.
     */
    fun loadTextureFromPixels(pixels: IntArray, width: Int, height: Int, generateMipmap: Boolean = true): Int {
        val bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        val texId = loadTextureFromBitmap(bitmap, generateMipmap)
        bitmap.recycle()
        return texId
    }

    /**
     * Inicializa las texturas requeridas para el motor 3D de Sanatorio en OpenGL ES 3.2.
     */
    fun initAllTextures(context: Context) {
        // 1. Textura del Piso: Prioridad 'dirty_tiles_diffuse.jpg'
        val floorBmp = loadBitmapFromAssets(context, "textures/dirty_tiles/dirty_tiles_diffuse.jpg", 128, 128)
            ?: loadBitmapFromAssets(context, "textures/interior_tiles/interior_tiles_diffuse.jpg", 128, 128)

        floorTextureId = if (floorBmp != null) {
            Log.i(TAG, "Textura de suelo cargada exitosamente desde asset dirty_tiles_diffuse.jpg")
            val id = loadTextureFromBitmap(floorBmp)
            floorBmp.recycle()
            id
        } else {
            Log.i(TAG, "Utilizando textura de suelo procedural de reserva")
            loadTextureFromPixels(TextureGenerator.floorTexture, TextureGenerator.TEX_SIZE, TextureGenerator.TEX_SIZE)
        }
        textureMap["floor"] = floorTextureId
    }

    /**
     * Obtiene una textura cacheada o 0 si no existe.
     */
    fun getTexture(name: String): Int = textureMap[name] ?: 0
}
