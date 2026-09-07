package com.example.sanatorio.engine.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.example.sanatorio.engine.TextureGenerator
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.Decoration
import com.example.sanatorio.model.DecorationType
import com.example.sanatorio.model.GameState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Renderizador de alta velocidad en OpenGL ES 3.2 para Sanatorio 3D.
 * Ejecuta en un hilo dedicado (GLThread) desacoplado del hilo de UI de Compose.
 * Soporta texturas PBR reales del piso (dirty_tiles), iluminación cónica física de linterna
 * con warm-up y parpadeos fluorescentes.
 */
class SanatorioGLRenderer(
    private val context: Context,
    private val gameState: GameState,
    private val map: AsylumMap
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "SanatorioGLRenderer"
        private const val INTERNAL_WIDTH = 480
        private const val INTERNAL_HEIGHT = 270

        // Quad a pantalla completa para el pipeline de sombreado OpenGL ES 3.2
        private val QUAD_VERTICES = floatArrayOf(
            -1.0f,  1.0f, 0.0f,  0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,  0.0f, 1.0f,
             1.0f,  1.0f, 0.0f,  1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,  1.0f, 1.0f
        )
    }

    private var programId: Int = 0
    private var uTextureLocation: Int = -1
    private var uFlashlightIntensityLocation: Int = -1
    private var uFlashlightWarmthLocation: Int = -1
    private var uSanityLocation: Int = -1
    private var uTimeLocation: Int = -1

    private var sceneTextureId: Int = 0
    private lateinit var vertexBuffer: FloatBuffer

    // Buffers directos nativos reutilizables (Cero asignaciones por fotograma)
    private val pixelBuffer: ByteBuffer = ByteBuffer.allocateDirect(INTERNAL_WIDTH * INTERNAL_HEIGHT * 4)
        .order(ByteOrder.nativeOrder())
    private val rawPixels = IntArray(INTERNAL_WIDTH * INTERNAL_HEIGHT)
    private val zBuffer = FloatArray(INTERNAL_WIDTH)

    // Textura cargada desde assets (dirty_tiles del sanatorio)
    private var floorTexturePixels: IntArray = TextureGenerator.floorTexture
    private val ceilingTexturePixels = TextureGenerator.ceilingTexture
    private val texSize = TextureGenerator.TEX_SIZE

    // Parámetros de encendido gradual de la linterna (Warm-up)
    private var currentFlashWarmup: Float = 0.0f
    private var frameTimeSec: Float = 0.0f

    init {
        loadFloorTextureFromAsset()
    }

    private fun loadFloorTextureFromAsset() {
        try {
            val stream = context.assets.open("textures/dirty_tiles/dirty_tiles_diffuse.jpg")
            val bmp = BitmapFactory.decodeStream(stream)
            stream.close()
            if (bmp != null) {
                val scaled = Bitmap.createScaledBitmap(bmp, texSize, texSize, true)
                val pixels = IntArray(texSize * texSize)
                scaled.getPixels(pixels, 0, texSize, 0, 0, texSize, texSize)
                floorTexturePixels = pixels
                Log.i(TAG, "Cargada textura 'dirty_tiles' en el pipeline de renderizado.")
                if (scaled != bmp) scaled.recycle()
                bmp.recycle()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Usando textura procedural de suelo: ${e.message}")
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES32.glClearColor(0.04f, 0.05f, 0.06f, 1.0f)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)

        // Shaders OpenGL ES 3.2
        val vertexShader = """#version 300 es
layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

out vec2 vTexCoord;

void main() {
    gl_Position = vec4(aPosition, 1.0);
    vTexCoord = aTexCoord;
}
"""

        val fragmentShader = """#version 300 es
precision highp float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uSceneTexture;
uniform float uFlashlightIntensity;
uniform float uFlashlightWarmth;
uniform float uSanity;
uniform float uTime;

void main() {
    vec2 uv = vTexCoord;

    // Distorsión y aberración cromática sutil cuando la cordura está baja
    float insanity = clamp((40.0 - uSanity) / 40.0, 0.0, 1.0);
    float chromAb = insanity * 0.006 * sin(uTime * 5.0 + uv.y * 20.0);

    float r = texture(uSceneTexture, vec2(uv.x + chromAb, uv.y)).r;
    float g = texture(uSceneTexture, uv).g;
    float b = texture(uSceneTexture, vec2(uv.x - chromAb, uv.y)).b;
    vec3 sceneColor = vec3(r, g, b);

    // Cono de iluminación de linterna lógica sobre la imagen
    vec2 center = vec2(0.5, 0.55);
    float dist = distance(uv, center);

    // Tono cálido de filamento incandescente vintage (1984)
    vec3 tungstenTint = vec3(1.0, 0.88, 0.65);
    
    // Halo central concentrado (spotlight beam) y caída de penumbra
    float spotMask = smoothstep(0.55, 0.12, dist);
    float spotBeam = spotMask * uFlashlightIntensity;
    
    // Destello de calentamiento de filamento (Flashlight turn-on warmup glow)
    vec3 lightBoost = tungstenTint * (spotBeam * 0.45 + (uFlashlightWarmth * spotMask * 0.3));
    
    // Viñeteado de claustrofobia del sanatorio
    float vignette = smoothstep(0.95, 0.35, dist);
    
    vec3 finalColor = (sceneColor + lightBoost) * vignette;

    // Grano analógico sutil de los 80s
    float noise = fract(sin(dot(uv * uTime, vec2(12.9898, 78.233))) * 43758.5453);
    finalColor += (noise - 0.5) * 0.025;

    fragColor = vec4(finalColor, 1.0);
}
"""

        programId = ShaderHelper.createProgram(vertexShader, fragmentShader)
        uTextureLocation = GLES32.glGetUniformLocation(programId, "uSceneTexture")
        uFlashlightIntensityLocation = GLES32.glGetUniformLocation(programId, "uFlashlightIntensity")
        uFlashlightWarmthLocation = GLES32.glGetUniformLocation(programId, "uFlashlightWarmth")
        uSanityLocation = GLES32.glGetUniformLocation(programId, "uSanity")
        uTimeLocation = GLES32.glGetUniformLocation(programId, "uTime")

        // Crear textura para volcado rápido de frame
        val textures = IntArray(1)
        GLES32.glGenTextures(1, textures, 0)
        sceneTextureId = textures[0]

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, sceneTextureId)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)

        // Asignar almacenamiento inicial para la textura en GPU
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA,
            INTERNAL_WIDTH, INTERNAL_HEIGHT, 0,
            GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, null
        )

        // Buffer de vértices
        vertexBuffer = ByteBuffer.allocateDirect(QUAD_VERTICES.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(QUAD_VERTICES)
        vertexBuffer.position(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        frameTimeSec += 0.016f

        // Cálculos de warm-up e intensidad de la linterna
        val targetIntensity = if (gameState.isFlashlightOn) gameState.flashlightFlickerFactor else 0.0f
        if (gameState.isFlashlightOn && currentFlashWarmup < 1.0f) {
            currentFlashWarmup = (currentFlashWarmup + 0.15f).coerceAtMost(1.0f)
        } else if (!gameState.isFlashlightOn) {
            currentFlashWarmup = 0.0f
        }

        // Trazado de escena 3D a nivel de píxeles
        renderSceneToBuffer()

        // Volcar el buffer a la GPU mediante glTexSubImage2D (operación de ancho de banda óptimo)
        pixelBuffer.position(0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, sceneTextureId)
        GLES32.glTexSubImage2D(
            GLES32.GL_TEXTURE_2D, 0, 0, 0,
            INTERNAL_WIDTH, INTERNAL_HEIGHT,
            GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, pixelBuffer
        )

        // Dibujar con el programa de shaders OpenGL ES 3.2
        GLES32.glUseProgram(programId)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, sceneTextureId)
        GLES32.glUniform1i(uTextureLocation, 0)

        GLES32.glUniform1f(uFlashlightIntensityLocation, targetIntensity)
        GLES32.glUniform1f(uFlashlightWarmthLocation, currentFlashWarmup)
        GLES32.glUniform1f(uSanityLocation, gameState.sanity)
        GLES32.glUniform1f(uTimeLocation, frameTimeSec)

        // Configuración de atributos de vértice
        vertexBuffer.position(0)
        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, 5 * 4, vertexBuffer)

        vertexBuffer.position(3)
        GLES32.glEnableVertexAttribArray(1)
        GLES32.glVertexAttribPointer(1, 2, GLES32.GL_FLOAT, false, 5 * 4, vertexBuffer)

        GLES32.glDrawArrays(GLES32.GL_TRIANGLE_STRIP, 0, 4)

        GLES32.glDisableVertexAttribArray(0)
        GLES32.glDisableVertexAttribArray(1)
    }

    private fun renderSceneToBuffer() {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY
        val planeX = gameState.planeX
        val planeY = gameState.planeY

        val pitchOffset = (gameState.pitch * (INTERNAL_HEIGHT * 0.4f)).toInt()
        val bobOffset = (gameState.headBob * 8f).toInt()
        val horizon = (INTERNAL_HEIGHT / 2) + pitchOffset + bobOffset

        val isFlashlight = gameState.isFlashlightOn
        val flashlightIntensity = if (isFlashlight) gameState.flashlightFlickerFactor else 0.0f
        val overheadFlicker = gameState.overheadLightFlicker
        val ambientBase = if (gameState.isBlackout) 0.02f else 0.08f

        // 1. Renderizado de Suelo (con textura dirty_tiles real) y Techo
        renderFloorAndCeiling(posX, posY, dirX, dirY, planeX, planeY, horizon, flashlightIntensity, ambientBase, overheadFlicker)

        // 2. Renderizado de Paredes (Raycasting DDA)
        for (x in 0 until INTERNAL_WIDTH) {
            val cameraX = 2.0 * x / INTERNAL_WIDTH - 1.0
            val rayDirX = dirX + planeX * cameraX
            val rayDirY = dirY + planeY * cameraX

            var mapX = posX.toInt()
            var mapY = posY.toInt()

            val deltaDistX = if (rayDirX == 0.0) 1e30 else abs(1.0 / rayDirX)
            val deltaDistY = if (rayDirY == 0.0) 1e30 else abs(1.0 / rayDirY)

            val stepX: Int
            var sideDistX: Double
            if (rayDirX < 0) {
                stepX = -1
                sideDistX = (posX - mapX) * deltaDistX
            } else {
                stepX = 1
                sideDistX = (mapX + 1.0 - posX) * deltaDistX
            }

            val stepY: Int
            var sideDistY: Double
            if (rayDirY < 0) {
                stepY = -1
                sideDistY = (posY - mapY) * deltaDistY
            } else {
                stepY = 1
                sideDistY = (mapY + 1.0 - posY) * deltaDistY
            }

            var hit = 0
            var side = 0
            var wallType = 1

            while (hit == 0) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                } else {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }

                if (map.isWall(mapX.toDouble(), mapY.toDouble())) {
                    hit = 1
                    wallType = map.getTile(mapX, mapY)
                }
            }

            val perpWallDist = if (side == 0) (sideDistX - deltaDistX) else (sideDistY - deltaDistY)
            zBuffer[x] = perpWallDist.toFloat()

            val lineHeight = (INTERNAL_HEIGHT / perpWallDist).toInt()
            val drawStart = (horizon - lineHeight / 2).coerceIn(0, INTERNAL_HEIGHT - 1)
            val drawEnd = (horizon + lineHeight / 2).coerceIn(0, INTERNAL_HEIGHT - 1)

            var wallX = if (side == 0) posY + perpWallDist * rayDirY else posX + perpWallDist * rayDirX
            wallX -= floor(wallX)

            var texX = (wallX * texSize).toInt()
            if (side == 0 && rayDirX > 0) texX = texSize - texX - 1
            if (side == 1 && rayDirY < 0) texX = texSize - texX - 1

            val texture = when (wallType) {
                AsylumMap.WALL_TILES -> TextureGenerator.tilesWall
                AsylumMap.WALL_PADDED -> TextureGenerator.paddedWall
                AsylumMap.WALL_BARS -> TextureGenerator.barsWall
                AsylumMap.WALL_DOOR -> TextureGenerator.doorWall
                AsylumMap.WALL_BLOOD -> TextureGenerator.bloodWall
                AsylumMap.WALL_BRICK -> TextureGenerator.brickWall
                else -> TextureGenerator.tilesWall
            }

            // Iluminación cónica lógica de linterna
            val colFactor = 1.0f - abs(cameraX.toFloat()) * 0.45f
            val flashlightFalloff = if (flashlightIntensity > 0f) {
                val distFactor = (1.0f - (perpWallDist.toFloat() / 9.0f)).coerceIn(0.0f, 1.0f)
                (distFactor * distFactor * colFactor * flashlightIntensity * 1.6f)
            } else 0f

            val fogFactor = (1.0f - (perpWallDist.toFloat() / 13.0f)).coerceIn(0.0f, 1.0f)
            val lampProximity = getLampIllumination(mapX.toDouble(), mapY.toDouble()) * overheadFlicker

            var totalLight = (ambientBase + flashlightFalloff + lampProximity) * fogFactor
            if (side == 1) totalLight *= 0.72f
            totalLight = totalLight.coerceIn(0.02f, 1.0f)

            val stepTex = 1.0 * texSize / lineHeight
            var texPos = (drawStart - horizon + lineHeight / 2.0) * stepTex

            for (y in drawStart..drawEnd) {
                val texY = (texPos.toInt()) and (texSize - 1)
                texPos += stepTex
                val color = texture[texY * texSize + texX]

                if ((color ushr 24) > 10) {
                    rawPixels[y * INTERNAL_WIDTH + x] = shadeColor(color, totalLight)
                }
            }
        }

        // 3. Renderizado de Decoraciones / Sprites
        renderDecorations(gameState, horizon, isFlashlight, flashlightIntensity, ambientBase, overheadFlicker)

        // 4. Actualizar objeto bajo el punto de mira para la interacción
        updateTargetDecoration(gameState)

        // Copiar a ByteBuffer en formato RGBA
        pixelBuffer.position(0)
        for (i in 0 until (INTERNAL_WIDTH * INTERNAL_HEIGHT)) {
            val c = rawPixels[i]
            val a = (c ushr 24) and 0xFF
            val r = (c ushr 16) and 0xFF
            val g = (c ushr 8) and 0xFF
            val b = c and 0xFF
            pixelBuffer.put(r.toByte())
            pixelBuffer.put(g.toByte())
            pixelBuffer.put(b.toByte())
            pixelBuffer.put(a.toByte())
        }
    }

    private fun renderFloorAndCeiling(
        posX: Double, posY: Double, dirX: Double, dirY: Double, planeX: Double, planeY: Double,
        horizon: Int, flashlightIntensity: Float, ambientBase: Float, overheadFlicker: Float
    ) {
        val rayDirX0 = dirX - planeX
        val rayDirY0 = dirY - planeY
        val rayDirX1 = dirX + planeX
        val rayDirY1 = dirY + planeY

        for (y in 0 until INTERNAL_HEIGHT) {
            val isFloor = y > horizon
            val p = if (isFloor) (y - horizon) else (horizon - y)
            if (p == 0) continue

            val posZ = 0.5 * INTERNAL_HEIGHT
            val rowDistance = posZ / p

            val floorStepX = rowDistance * (rayDirX1 - rayDirX0) / INTERNAL_WIDTH
            val floorStepY = rowDistance * (rayDirY1 - rayDirY0) / INTERNAL_WIDTH

            var floorX = posX + rowDistance * rayDirX0
            var floorY = posY + rowDistance * rayDirY0

            // Textura del piso: dirty_tiles del sanatorio
            val texture = if (isFloor) floorTexturePixels else ceilingTexturePixels
            val fog = (1.0f - (rowDistance.toFloat() / 10.0f)).coerceIn(0.0f, 1.0f)

            for (x in 0 until INTERNAL_WIDTH) {
                val cellX = floorX.toInt()
                val cellY = floorY.toInt()

                val tx = ((floorX - cellX) * texSize).toInt() and (texSize - 1)
                val ty = ((floorY - cellY) * texSize).toInt() and (texSize - 1)

                floorX += floorStepX
                floorY += floorStepY

                val cameraX = 2.0 * x / INTERNAL_WIDTH - 1.0
                val colFactor = (1.0f - abs(cameraX.toFloat()) * 0.5f).coerceIn(0f, 1f)
                val distFactor = (1.0f - (rowDistance.toFloat() / 8.0f)).coerceIn(0f, 1f)
                val flash = if (flashlightIntensity > 0f) distFactor * distFactor * colFactor * flashlightIntensity * 1.4f else 0f
                val lamp = getLampIllumination(floorX, floorY) * overheadFlicker

                val totalLight = ((ambientBase + flash + lamp) * fog).coerceIn(0.02f, 1.0f)
                val color = texture[ty * texSize + tx]
                rawPixels[y * INTERNAL_WIDTH + x] = shadeColor(color, totalLight)
            }
        }
    }

    private fun renderDecorations(
        gameState: GameState, horizon: Int,
        isFlashlight: Boolean, flashlightIntensity: Float, ambientBase: Float, overheadFlicker: Float
    ) {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY
        val planeX = gameState.planeX
        val planeY = gameState.planeY

        val sortedDecorations = map.decorations.sortedByDescending {
            (posX - it.x) * (posX - it.x) + (posY - it.y) * (posY - it.y)
        }

        for (dec in sortedDecorations) {
            val spriteX = dec.x - posX
            val spriteY = dec.y - posY

            val invDet = 1.0 / (planeX * dirY - dirX * planeY)
            val transformX = invDet * (dirY * spriteX - dirX * spriteY)
            val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

            if (transformY <= 0.2) continue

            val spriteScreenX = ((INTERNAL_WIDTH / 2) * (1 + transformX / transformY)).toInt()
            val spriteHeight = abs((INTERNAL_HEIGHT / transformY).toInt())
            val drawStartY = (horizon - spriteHeight / 2).coerceIn(0, INTERNAL_HEIGHT - 1)
            val drawEndY = (horizon + spriteHeight / 2).coerceIn(0, INTERNAL_HEIGHT - 1)

            val spriteWidth = abs((INTERNAL_HEIGHT / transformY).toInt())
            val drawStartX = (spriteScreenX - spriteWidth / 2).coerceIn(0, INTERNAL_WIDTH - 1)
            val drawEndX = (spriteScreenX + spriteWidth / 2).coerceIn(0, INTERNAL_WIDTH - 1)

            val spriteTex = TextureGenerator.getSpriteTexture(dec.type)

            val fog = (1.0f - (transformY.toFloat() / 11.0f)).coerceIn(0.0f, 1.0f)
            val flash = if (flashlightIntensity > 0f) {
                val fDist = (1.0f - (transformY.toFloat() / 8.5f)).coerceIn(0f, 1f)
                fDist * fDist * flashlightIntensity * 1.6f
            } else 0f
            val lamp = getLampIllumination(dec.x, dec.y) * overheadFlicker
            val light = ((ambientBase + flash + lamp) * fog).coerceIn(0.02f, 1.0f)

            for (stripe in drawStartX..drawEndX) {
                val texX = ((stripe - (spriteScreenX - spriteWidth / 2)) * texSize / spriteWidth).toInt()
                if (transformY < zBuffer[stripe]) {
                    for (y in drawStartY..drawEndY) {
                        val d = (y - horizon + spriteHeight / 2)
                        val texY = ((d * texSize) / spriteHeight).coerceIn(0, texSize - 1)
                        val color = spriteTex[texY * texSize + texX]

                        if ((color ushr 24) > 40) {
                            rawPixels[y * INTERNAL_WIDTH + stripe] = shadeColor(color, light)
                        }
                    }
                }
            }
        }
    }

    private fun updateTargetDecoration(gameState: GameState) {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY

        var closestDec: Decoration? = null
        var closestDist = 2.4

        for (dec in map.decorations) {
            val dx = dec.x - posX
            val dy = dec.y - posY
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < closestDist) {
                val normX = dx / dist
                val normY = dy / dist
                val dot = normX * dirX + normY * dirY
                if (dot > 0.85) {
                    closestDist = dist
                    closestDec = dec
                }
            }
        }

        gameState.targetDecoration = closestDec
    }

    private fun getLampIllumination(x: Double, y: Double): Float {
        var maxIllum = 0f
        for (dec in map.decorations) {
            if (dec.type == DecorationType.FLICKERING_LAMP) {
                val dx = dec.x - x
                val dy = dec.y - y
                val d2 = (dx * dx + dy * dy).toFloat()
                if (d2 < 16.0f) {
                    val illum = (1.0f - (d2 / 16.0f)).coerceIn(0.0f, 1.0f)
                    if (illum > maxIllum) maxIllum = illum
                }
            }
        }
        return maxIllum
    }

    private fun shadeColor(color: Int, factor: Float): Int {
        val a = (color ushr 24) and 0xFF
        val r = (((color ushr 16) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = (((color ushr 8) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
