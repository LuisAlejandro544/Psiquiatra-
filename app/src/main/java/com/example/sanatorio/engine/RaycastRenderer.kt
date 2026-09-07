package com.example.sanatorio.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.Decoration
import com.example.sanatorio.model.GameState
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class RaycastRenderer(
    val width: Int = 360,
    val height: Int = 200
) {
    val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val pixels = IntArray(width * height)
    private val zBuffer = FloatArray(width)

    // Cached references
    private val texSize = TextureGenerator.TEX_SIZE
    private val tilesWall = TextureGenerator.tilesWall
    private val paddedWall = TextureGenerator.paddedWall
    private val barsWall = TextureGenerator.barsWall
    private val doorWall = TextureGenerator.doorWall
    private val bloodWall = TextureGenerator.bloodWall
    private val brickWall = TextureGenerator.brickWall
    private val floorTex = TextureGenerator.floorTexture
    private val ceilingTex = TextureGenerator.ceilingTexture

    fun render(gameState: GameState, map: AsylumMap): Bitmap {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY
        val planeX = gameState.planeX
        val planeY = gameState.planeY

        val pitchOffset = (gameState.pitch * (height * 0.4f)).toInt()
        val bobOffset = (gameState.headBob * 8f).toInt()
        val horizon = (height / 2) + pitchOffset + bobOffset

        val isFlashlight = gameState.isFlashlightOn
        val flashlightIntensity = if (isFlashlight) gameState.flashlightFlickerFactor else 0.0f
        val overheadFlicker = gameState.overheadLightFlicker

        // Clear or prepare frame
        val ambientBase = if (gameState.isBlackout) 0.02f else 0.07f

        // 1. FLOOR & CEILING CASTING
        renderFloorAndCeiling(posX, posY, dirX, dirY, planeX, planeY, horizon, flashlightIntensity, ambientBase, overheadFlicker, map)

        // 2. WALL CASTING (DDA)
        for (x in 0 until width) {
            // Camera x coordinate in camera space (-1 to +1)
            val cameraX = 2.0 * x / width - 1.0
            val rayDirX = dirX + planeX * cameraX
            val rayDirY = dirY + planeY * cameraX

            var mapX = posX.toInt()
            var mapY = posY.toInt()

            val deltaDistX = if (rayDirX == 0.0) 1e30 else abs(1.0 / rayDirX)
            val deltaDistY = if (rayDirY == 0.0) 1e30 else abs(1.0 / rayDirY)

            val stepX: Int
            val stepY: Int
            var sideDistX: Double
            var sideDistY: Double

            if (rayDirX < 0) {
                stepX = -1
                sideDistX = (posX - mapX) * deltaDistX
            } else {
                stepX = 1
                sideDistX = (mapX + 1.0 - posX) * deltaDistX
            }

            if (rayDirY < 0) {
                stepY = -1
                sideDistY = (posY - mapY) * deltaDistY
            } else {
                stepY = 1
                sideDistY = (mapY + 1.0 - posY) * deltaDistY
            }

            var hit = false
            var side = 0 // 0 for X, 1 for Y
            var hitTile = 1
            var steps = 0

            while (!hit && steps < 30) {
                steps++
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX
                    mapX += stepX
                    side = 0
                } else {
                    sideDistY += deltaDistY
                    mapY += stepY
                    side = 1
                }

                hitTile = map.getTile(mapX, mapY)
                if (hitTile != AsylumMap.EMPTY) {
                    hit = true
                }
            }

            // Calculate distance projected on camera direction
            val perpWallDist = if (side == 0) {
                (mapX - posX + (1 - stepX) / 2.0) / rayDirX
            } else {
                (mapY - posY + (1 - stepY) / 2.0) / rayDirY
            }.coerceAtLeast(0.1)

            zBuffer[x] = perpWallDist.toFloat()

            // Calculate line height on screen
            val lineHeight = ((height / perpWallDist)).toInt()
            val drawStart = max(0, -lineHeight / 2 + horizon)
            val drawEnd = min(height - 1, lineHeight / 2 + horizon)

            // Texture mapping
            var wallX = if (side == 0) posY + perpWallDist * rayDirY else posX + perpWallDist * rayDirX
            wallX -= floor(wallX)

            var texX = (wallX * texSize).toInt()
            if (side == 0 && rayDirX > 0) texX = texSize - texX - 1
            if (side == 1 && rayDirY < 0) texX = texSize - texX - 1

            val texture = when (hitTile) {
                AsylumMap.WALL_PADDED -> paddedWall
                AsylumMap.WALL_BARS -> barsWall
                AsylumMap.WALL_DOOR -> doorWall
                AsylumMap.WALL_BLOOD -> bloodWall
                AsylumMap.WALL_BRICK -> brickWall
                else -> tilesWall
            }

            // Lighting computation
            // Angle between center ray (x = width/2) and this ray
            val colFactor = 1.0f - abs(cameraX.toFloat()) * 0.45f // center beam is strongest
            val flashlightFalloff = if (flashlightIntensity > 0f) {
                val distFactor = (1.0f - (perpWallDist.toFloat() / 8.5f)).coerceIn(0.0f, 1.0f)
                (distFactor * distFactor * colFactor * flashlightIntensity * 1.5f)
            } else 0f

            // Distance fog
            val fogFactor = (1.0f - (perpWallDist.toFloat() / 12.0f)).coerceIn(0.0f, 1.0f)
            
            // Fluorescent flicker contribution if near lamp
            val lampProximity = getLampIllumination(mapX.toDouble(), mapY.toDouble(), map) * overheadFlicker

            var totalLight = (ambientBase + flashlightFalloff + lampProximity) * fogFactor
            if (side == 1) totalLight *= 0.72f // Shadow on Y-sides

            totalLight = totalLight.coerceIn(0.02f, 1.0f)

            val stepTex = 1.0 * texSize / lineHeight
            var texPos = (drawStart - horizon + lineHeight / 2.0) * stepTex

            for (y in drawStart..drawEnd) {
                val texY = (texPos.toInt()) and (texSize - 1)
                texPos += stepTex
                val color = texture[texY * texSize + texX]

                if ((color ushr 24) > 10) { // Check transparency for iron bars
                    pixels[y * width + x] = shadeColor(color, totalLight)
                }
            }
        }

        // 3. SPRITE CASTING (Decorations)
        renderDecorations(gameState, map, horizon, isFlashlight, flashlightIntensity, ambientBase, overheadFlicker)

        // 4. Update Target Decoration in crosshair
        updateTargetDecoration(gameState, map)

        // Push pixels into bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun renderFloorAndCeiling(
        posX: Double, posY: Double, dirX: Double, dirY: Double, planeX: Double, planeY: Double,
        horizon: Int, flashlightIntensity: Float, ambientBase: Float, overheadFlicker: Float, map: AsylumMap
    ) {
        val rayDirX0 = dirX - planeX
        val rayDirY0 = dirY - planeY
        val rayDirX1 = dirX + planeX
        val rayDirY1 = dirY + planeY

        for (y in 0 until height) {
            val isFloor = y > horizon
            val p = if (isFloor) (y - horizon) else (horizon - y)
            if (p == 0) continue

            val posZ = 0.5 * height
            val rowDistance = posZ / p

            val floorStepX = rowDistance * (rayDirX1 - rayDirX0) / width
            val floorStepY = rowDistance * (rayDirY1 - rayDirY0) / width

            var floorX = posX + rowDistance * rayDirX0
            var floorY = posY + rowDistance * rayDirY0

            val texture = if (isFloor) floorTex else ceilingTex
            val fog = (1.0f - (rowDistance.toFloat() / 9.0f)).coerceIn(0.0f, 1.0f)

            for (x in 0 until width) {
                val cellX = floorX.toInt()
                val cellY = floorY.toInt()

                val tx = ((floorX - cellX) * texSize).toInt() and (texSize - 1)
                val ty = ((floorY - cellY) * texSize).toInt() and (texSize - 1)

                floorX += floorStepX
                floorY += floorStepY

                val cameraX = 2.0 * x / width - 1.0
                val colFactor = (1.0f - abs(cameraX.toFloat()) * 0.5f).coerceIn(0f, 1f)
                val distFactor = (1.0f - (rowDistance.toFloat() / 7.5f)).coerceIn(0f, 1f)
                val flash = if (flashlightIntensity > 0f) distFactor * distFactor * colFactor * flashlightIntensity * 1.3f else 0f
                val lamp = getLampIllumination(floorX, floorY, map) * overheadFlicker

                val totalLight = ((ambientBase + flash + lamp) * fog).coerceIn(0.02f, 1.0f)
                val color = texture[ty * texSize + tx]
                pixels[y * width + x] = shadeColor(color, totalLight)
            }
        }
    }

    private fun renderDecorations(
        gameState: GameState, map: AsylumMap, horizon: Int,
        isFlashlight: Boolean, flashlightIntensity: Float, ambientBase: Float, overheadFlicker: Float
    ) {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY
        val planeX = gameState.planeX
        val planeY = gameState.planeY

        // Sort decorations by distance (descending)
        val sortedSprites = map.decorations.map { decor ->
            val dist = (posX - decor.x) * (posX - decor.x) + (posY - decor.y) * (posY - decor.y)
            Pair(decor, dist)
        }.sortedByDescending { it.second }

        val invDet = 1.0 / (planeX * dirY - dirX * planeY)

        for ((decor, _) in sortedSprites) {
            val spriteX = decor.x - posX
            val spriteY = decor.y - posY

            // Transform sprite with the inverse camera matrix
            val transformX = invDet * (dirY * spriteX - dirX * spriteY)
            val transformY = invDet * (-planeY * spriteX + planeX * spriteY)

            if (transformY <= 0.1) continue // Behind camera

            val spriteScreenX = ((width / 2) * (1 + transformX / transformY)).toInt()
            val spriteHeight = abs((height / transformY)).toInt()
            val spriteWidth = abs((height / transformY)).toInt()

            // Vertical position
            val vOffset = if (decor.type == com.example.sanatorio.model.DecorationType.FLICKERING_LAMP) {
                // Ceiling mounted
                (-spriteHeight * 0.45).toInt()
            } else {
                // Floor mounted
                (spriteHeight * 0.25).toInt()
            }

            val drawStartY = max(0, -spriteHeight / 2 + horizon + vOffset)
            val drawEndY = min(height - 1, spriteHeight / 2 + horizon + vOffset)

            val drawStartX = max(0, -spriteWidth / 2 + spriteScreenX)
            val drawEndX = min(width - 1, spriteWidth / 2 + spriteScreenX)

            val spriteTex = TextureGenerator.getSpriteTexture(decor.type)
            val dist = sqrt(spriteX * spriteX + spriteY * spriteY).toFloat()
            val fog = (1.0f - (dist / 11.0f)).coerceIn(0.0f, 1.0f)
            val flash = if (flashlightIntensity > 0f) {
                val fDist = (1.0f - (dist / 8.0f)).coerceIn(0.0f, 1.0f)
                fDist * fDist * flashlightIntensity * 1.6f
            } else 0f
            val lamp = getLampIllumination(decor.x, decor.y, map) * overheadFlicker
            val light = ((ambientBase + flash + lamp) * fog).coerceIn(0.03f, 1.1f)

            for (stripe in drawStartX..drawEndX) {
                val texX = ((stripe - (-spriteWidth / 2 + spriteScreenX)) * texSize / spriteWidth).coerceIn(0, texSize - 1)

                // Depth test
                if (transformY < zBuffer[stripe]) {
                    for (y in drawStartY..drawEndY) {
                        val d = (y - (horizon + vOffset)) * 256 - spriteHeight * 128
                        val texY = ((d * texSize) / (spriteHeight * 256)).coerceIn(0, texSize - 1)
                        val color = spriteTex[texY * texSize + texX]

                        val alpha = (color ushr 24)
                        if (alpha > 20) {
                            pixels[y * width + stripe] = shadeColor(color, light)
                        }
                    }
                }
            }
        }
    }

    private fun updateTargetDecoration(gameState: GameState, map: AsylumMap) {
        val posX = gameState.playerX
        val posY = gameState.playerY
        val dirX = gameState.dirX
        val dirY = gameState.dirY

        var closest: Decoration? = null
        var closestDist = 2.2 // Interaction distance threshold

        for (decor in map.decorations) {
            if (!decor.canInspect) continue
            val dx = decor.x - posX
            val dy = decor.y - posY
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < closestDist) {
                // Check dot product to see if player is looking towards it
                val dot = (dx * dirX + dy * dirY) / dist
                if (dot > 0.82) { // within ~35 degree cone of vision
                    closestDist = dist
                    closest = decor
                }
            }
        }

        gameState.targetDecoration = closest
    }

    private fun getLampIllumination(x: Double, y: Double, map: AsylumMap): Float {
        var total = 0.0f
        for (decor in map.decorations) {
            if (decor.isLightEmitter) {
                val dx = decor.x - x
                val dy = decor.y - y
                val d2 = dx * dx + dy * dy
                if (d2 < 18.0) {
                    val falloff = (1.0f - (sqrt(d2).toFloat() / 4.2f)).coerceIn(0.0f, 1.0f)
                    total += falloff * 0.85f
                }
            }
        }
        return total.coerceAtMost(1.0f)
    }

    private fun shadeColor(color: Int, factor: Float): Int {
        val a = (color ushr 24) and 0xFF
        val r = (((color ushr 16) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = (((color ushr 8) and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
