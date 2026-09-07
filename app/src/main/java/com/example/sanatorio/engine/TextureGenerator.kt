package com.example.sanatorio.engine

import android.graphics.Color
import com.example.sanatorio.model.DecorationType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object TextureGenerator {
    const val TEX_SIZE = 64

    // Wall textures
    val tilesWall: IntArray by lazy { generateTilesWall() }
    val paddedWall: IntArray by lazy { generatePaddedWall() }
    val barsWall: IntArray by lazy { generateBarsWall() }
    val doorWall: IntArray by lazy { generateDoorWall() }
    val bloodWall: IntArray by lazy { generateBloodWall() }
    val brickWall: IntArray by lazy { generateBrickWall() }

    // Surface textures
    val floorTexture: IntArray by lazy { generateFloorTexture() }
    val ceilingTexture: IntArray by lazy { generateCeilingTexture() }

    // Sprites
    private val spriteCache: MutableMap<DecorationType, IntArray> = mutableMapOf()

    fun getSpriteTexture(type: DecorationType): IntArray {
        return spriteCache.getOrPut(type) {
            when (type) {
                DecorationType.WHEELCHAIR -> generateWheelchairSprite()
                DecorationType.GURNEY -> generateGurneySprite()
                DecorationType.IV_STAND -> generateIVStandSprite()
                DecorationType.PATIENT_FILE -> generatePatientFileSprite()
                DecorationType.MEDICINE_CART -> generateMedicineCartSprite()
                DecorationType.BLOOD_MARK -> generateBloodMarkSprite()
                DecorationType.FLICKERING_LAMP -> generateFlickeringLampSprite()
                DecorationType.OLD_DESK -> generateOldDeskSprite()
            }
        }
    }

    private fun generateTilesWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isGrout = (x % 16 == 0) || (y % 16 == 0)
                val noise = ((sin(x * 0.4) * sin(y * 0.4) + 1.0) * 12.0).toInt()
                val crack = if ((x * 3 + y * 7) % 43 == 0 && x > 10 && x < 54) 30 else 0
                val mold = if ((x in 12..35 && y in 38..58)) 25 else 0

                pixels[y * TEX_SIZE + x] = if (isGrout) {
                    Color.rgb(18, 25, 22)
                } else {
                    val r = (65 + noise - crack - mold).coerceIn(15, 255)
                    val g = (85 + noise - crack).coerceIn(20, 255)
                    val b = (78 + noise - crack - mold).coerceIn(18, 255)
                    Color.rgb(r, g, b)
                }
            }
        }
        return pixels
    }

    private fun generatePaddedWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                // Diamond pattern
                val dx = (x % 32) - 16
                val dy = (y % 32) - 16
                val diamondDist = abs(dx) + abs(dy)
                val isStitch = abs(diamondDist - 16) <= 1
                val isButton = (abs(dx) <= 2 && abs(dy) <= 2)

                val leatherNoise = ((sin(x * 0.3) + sin(y * 0.5) + 2.0) * 8.0).toInt()
                val depthShading = (16 - diamondDist) * 3

                val baseR = (50 + depthShading + leatherNoise).coerceIn(15, 110)
                val baseG = (42 + depthShading + leatherNoise).coerceIn(12, 100)
                val baseB = (38 + depthShading + leatherNoise).coerceIn(10, 95)

                pixels[y * TEX_SIZE + x] = when {
                    isButton -> Color.rgb(20, 16, 14)
                    isStitch -> Color.rgb(25, 20, 18)
                    else -> Color.rgb(baseR, baseG, baseB)
                }
            }
        }
        return pixels
    }

    private fun generateBarsWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val barIndex = x % 16
                val isVerticalBar = barIndex in 6..9
                val isHorizontalCrossbar = (y in 14..17) || (y in 46..49)

                if (isVerticalBar || isHorizontalCrossbar) {
                    val rustNoise = ((sin(x * 0.8) * cos(y * 0.8) + 1.0) * 15.0).toInt()
                    val highlight = if (barIndex == 7 || y == 15 || y == 47) 35 else 0
                    val r = (90 + highlight + rustNoise).coerceIn(20, 180)
                    val g = (60 + highlight / 2).coerceIn(15, 120)
                    val b = (50 + highlight / 3).coerceIn(15, 100)
                    pixels[y * TEX_SIZE + x] = Color.rgb(r, g, b)
                } else {
                    // Transparent bar opening
                    pixels[y * TEX_SIZE + x] = 0x00000000
                }
            }
        }
        return pixels
    }

    private fun generateDoorWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isBorder = x in 0..4 || x in 59..63 || y in 0..4 || y in 60..63
                val isWindow = (x in 22..41) && (y in 12..28)
                val isHandle = (x in 8..14) && (y in 32..36)

                val woodGrain = ((sin(y * 0.6) + sin(x * 0.1) + 2.0) * 10.0).toInt()

                pixels[y * TEX_SIZE + x] = when {
                    isBorder -> Color.rgb(30, 36, 38)
                    isWindow -> {
                        // Glass with wire mesh
                        val wire = (x % 4 == 0) || (y % 4 == 0)
                        if (wire) Color.rgb(20, 24, 25) else Color.rgb(55, 75, 80)
                    }
                    isHandle -> Color.rgb(160, 140, 70) // tarnished brass
                    else -> {
                        val r = (60 + woodGrain).coerceIn(20, 120)
                        val g = (50 + woodGrain).coerceIn(15, 100)
                        val b = (45 + woodGrain).coerceIn(15, 95)
                        Color.rgb(r, g, b)
                    }
                }
            }
        }
        return pixels
    }

    private fun generateBloodWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val noise = ((sin(x * 0.5) * sin(y * 0.5) + 1.0) * 14.0).toInt()
                // Dripping blood pattern
                val isBloodSplat = (x in 18..44 && y in 14..32) && ((sin(x * 0.8) + cos(y * 0.7)) > -0.3)
                val isBloodDrip = (x == 24 || x == 32 || x == 39) && (y in 30..54)

                val baseR = 48 + noise
                val baseG = 52 + noise
                val baseB = 50 + noise

                pixels[y * TEX_SIZE + x] = if (isBloodSplat || isBloodDrip) {
                    val darkBlood = if (y > 40) 25 else 45
                    Color.rgb(130 + darkBlood, 12, 20)
                } else {
                    Color.rgb(baseR, baseG, baseB)
                }
            }
        }
        return pixels
    }

    private fun generateBrickWall(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            val row = y / 8
            val offsetX = if (row % 2 == 1) 8 else 0
            for (x in 0 until TEX_SIZE) {
                val isMortar = (y % 8 == 0) || ((x + offsetX) % 16 == 0)
                val noise = ((sin(x * 0.7) + cos(y * 0.9) + 2.0) * 10.0).toInt()
                pixels[y * TEX_SIZE + x] = if (isMortar) {
                    Color.rgb(40, 42, 44)
                } else {
                    Color.rgb((90 + noise).coerceIn(30, 160), (45 + noise / 2).coerceIn(15, 90), (38 + noise / 2).coerceIn(15, 80))
                }
            }
        }
        return pixels
    }

    private fun generateFloorTexture(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val check = ((x / 16) + (y / 16)) % 2 == 0
                val dirt = ((sin(x * 0.3) * cos(y * 0.3) + 1.0) * 8.0).toInt()
                val r = if (check) 40 + dirt else 24 + dirt
                val g = if (check) 44 + dirt else 26 + dirt
                val b = if (check) 42 + dirt else 25 + dirt
                pixels[y * TEX_SIZE + x] = Color.rgb(r, g, b)
            }
        }
        return pixels
    }

    private fun generateCeilingTexture(): IntArray {
        val pixels = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isConduit = (x == 32) || (y == 32)
                val noise = ((sin(x * 0.2) + cos(y * 0.2) + 2.0) * 4.0).toInt()
                pixels[y * TEX_SIZE + x] = if (isConduit) {
                    Color.rgb(18, 22, 24)
                } else {
                    val c = 16 + noise
                    Color.rgb(c, c + 2, c + 1)
                }
            }
        }
        return pixels
    }

    // ==========================================
    // Sprites Generator (with Alpha channel)
    // ==========================================

    private fun generateWheelchairSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                // Wheels at bottom left & right
                val leftWheelDist = sqrt(((x - 22) * (x - 22) + (y - 48) * (y - 48)).toDouble())
                val rightWheelDist = sqrt(((x - 42) * (x - 42) + (y - 48) * (y - 48)).toDouble())
                val isWheelRim = (leftWheelDist in 11.0..13.0) || (rightWheelDist in 11.0..13.0)
                val isSpoke = (leftWheelDist < 12.0 && (abs(x - 22) <= 1 || abs(y - 48) <= 1)) ||
                              (rightWheelDist < 12.0 && (abs(x - 42) <= 1 || abs(y - 48) <= 1))
                
                // Seat & Backrest
                val isSeat = (x in 20..44) && (y in 36..39)
                val isBackrest = (x in 23..41) && (y in 18..35)
                val isHandles = ((x in 19..22) || (x in 42..45)) && (y in 14..18)
                val isFrame = ((x in 20..23) || (x in 41..44)) && (y in 39..48)

                p[y * TEX_SIZE + x] = when {
                    isHandles -> Color.argb(255, 30, 30, 30) // rubber grips
                    isBackrest -> Color.argb(255, 80, 45, 35) // cracked brown leather
                    isSeat -> Color.argb(255, 70, 40, 30)
                    isFrame -> Color.argb(255, 120, 110, 100) // metal tubing
                    isWheelRim || isSpoke -> Color.argb(255, 140, 100, 70) // rusted steel
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generateGurneySprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isMattress = (x in 10..54) && (y in 32..38)
                val isSheet = (x in 12..52) && (y in 30..33)
                val isStrap = ((x in 20..22) || (x in 34..36) || (x in 44..46)) && (y in 29..39)
                val isLegs = ((x in 14..17) || (x in 47..50)) && (y in 39..56)
                val isCasters = ((x in 13..18) || (x in 46..51)) && (y in 56..60)

                p[y * TEX_SIZE + x] = when {
                    isStrap -> Color.argb(255, 45, 25, 15) // thick leather straps
                    isSheet -> Color.argb(255, 165, 160, 150) // stained hospital sheet
                    isMattress -> Color.argb(255, 70, 75, 72)
                    isLegs -> Color.argb(255, 110, 115, 120) // surgical steel
                    isCasters -> Color.argb(255, 40, 40, 40)
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generateIVStandSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isPole = (x in 31..33) && (y in 8..58)
                val isBase = (y in 58..62) && (abs(x - 32) <= (y - 58) * 3)
                val isHanger = (y in 8..11) && (x in 24..40)
                val isBag = (x in 23..29) && (y in 12..25)
                val isFluid = (x in 24..28) && (y in 17..24)
                val isTube = (x == 26) && (y in 25..46)

                p[y * TEX_SIZE + x] = when {
                    isFluid -> Color.argb(230, 130, 20, 25) // dark degraded fluid
                    isBag -> Color.argb(180, 190, 210, 220) // transparent IV bag
                    isTube -> Color.argb(190, 180, 50, 50)
                    isPole || isHanger || isBase -> Color.argb(255, 140, 145, 150)
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generatePatientFileSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isBoard = (x in 18..46) && (y in 20..56)
                val isClip = (x in 27..37) && (y in 16..22)
                val isPaper = (x in 20..44) && (y in 23..54)
                val isRedCross = ((x in 30..34 && y in 26..30) || (x in 31..33 && y in 25..31))
                val isLines = (y in 34..50 step 3) && (x in 23..41)

                p[y * TEX_SIZE + x] = when {
                    isClip -> Color.argb(255, 170, 175, 180)
                    isRedCross -> Color.argb(255, 180, 25, 25)
                    isLines -> Color.argb(255, 50, 50, 60)
                    isPaper -> Color.argb(255, 225, 218, 195) // aged parchment paper
                    isBoard -> Color.argb(255, 110, 75, 45) // masonite clipboard
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generateMedicineCartSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isTopTray = (x in 16..48) && (y in 28..31)
                val isBottomTray = (x in 16..48) && (y in 44..47)
                val isLegs = ((x in 17..19) || (x in 45..47)) && (y in 31..56)
                val isWheels = ((x in 16..20) || (x in 44..48)) && (y in 56..60)
                val isBottles = (y in 20..27) && ((x in 21..25) || (x in 29..33) || (x in 37..41))

                p[y * TEX_SIZE + x] = when {
                    isBottles -> if (x in 21..25) Color.argb(240, 160, 90, 30) // amber glass
                                 else Color.argb(240, 50, 140, 110) // medicine vial
                    isTopTray || isBottomTray -> Color.argb(255, 150, 155, 160)
                    isLegs -> Color.argb(255, 120, 125, 130)
                    isWheels -> Color.argb(255, 35, 35, 35)
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generateBloodMarkSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val dist = sqrt(((x - 32) * (x - 32) + (y - 32) * (y - 32)).toDouble())
                val noise = sin(x * 0.6) * cos(y * 0.6)
                if (dist < 18.0 + noise * 5.0) {
                    val alpha = ((1.0 - (dist / 24.0)) * 240).toInt().coerceIn(0, 255)
                    p[y * TEX_SIZE + x] = Color.argb(alpha, 130, 15, 18)
                } else {
                    p[y * TEX_SIZE + x] = 0x00000000
                }
            }
        }
        return p
    }

    private fun generateFlickeringLampSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isFixture = (x in 12..52) && (y in 6..11)
                val isTube = (x in 16..48) && (y in 11..16)
                val isHangerWires = ((x == 20) || (x == 44)) && (y in 0..6)
                val isGlowHalo = (x in 10..54) && (y in 12..28)

                p[y * TEX_SIZE + x] = when {
                    isTube -> Color.argb(255, 230, 250, 255) // bright fluorescent tube
                    isFixture -> Color.argb(255, 55, 60, 65)
                    isHangerWires -> Color.argb(255, 30, 30, 30)
                    isGlowHalo -> {
                        val falloff = (28 - y) * 8
                        Color.argb(falloff.coerceIn(0, 100), 180, 230, 255)
                    }
                    else -> 0x00000000
                }
            }
        }
        return p
    }

    private fun generateOldDeskSprite(): IntArray {
        val p = IntArray(TEX_SIZE * TEX_SIZE)
        for (y in 0 until TEX_SIZE) {
            for (x in 0 until TEX_SIZE) {
                val isDesktop = (x in 10..54) && (y in 30..35)
                val isPedestals = ((x in 12..22) || (x in 42..52)) && (y in 35..58)
                val isTypewriter = (x in 26..38) && (y in 22..29)
                val isPapers = (x in 41..49) && (y in 27..30)

                p[y * TEX_SIZE + x] = when {
                    isTypewriter -> Color.argb(255, 30, 32, 35) // black metal typewriter
                    isPapers -> Color.argb(255, 210, 205, 190)
                    isDesktop -> Color.argb(255, 85, 50, 30) // dark walnut
                    isPedestals -> Color.argb(255, 65, 38, 24)
                    else -> 0x00000000
                }
            }
        }
        return p
    }
}
