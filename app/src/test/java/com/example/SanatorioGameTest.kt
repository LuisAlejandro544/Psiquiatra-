package com.example

import com.example.sanatorio.engine.RaycastRenderer
import com.example.sanatorio.engine.TextureGenerator
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.DecorationType
import com.example.sanatorio.model.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SanatorioGameTest {

    @Test
    fun testAsylumMapLayoutAndCollisions() {
        val map = AsylumMap()
        // Borders should be walls
        assertTrue("Outer border must be solid wall", map.isWall(0.0, 0.0))
        assertTrue("Outer border must be solid wall", map.isWall(19.0, 19.0))
        
        // Spawn point at corridor should be walkable
        assertTrue("Corridor at (6,6) should be empty floor", !map.isWall(6.0, 6.0))
        
        // Room name identification
        val roomName = map.getRoomName(6.0, 6.0)
        assertTrue("Room name should identify corridor", roomName.contains("Pasillo"))

        // Verify decorations are loaded
        assertTrue("Must have decorations", map.decorations.isNotEmpty())
        assertTrue("Must contain flickering lamp", map.decorations.any { it.type == DecorationType.FLICKERING_LAMP })
        assertTrue("Must contain wheelchair", map.decorations.any { it.type == DecorationType.WHEELCHAIR })
    }

    @Test
    fun testTextureGenerator() {
        val tiles = TextureGenerator.tilesWall
        assertEquals(TextureGenerator.TEX_SIZE * TextureGenerator.TEX_SIZE, tiles.size)

        val wheelchairSprite = TextureGenerator.getSpriteTexture(DecorationType.WHEELCHAIR)
        assertEquals(TextureGenerator.TEX_SIZE * TextureGenerator.TEX_SIZE, wheelchairSprite.size)
    }

    @Test
    fun testGameStateAndRenderer() {
        val gameState = GameState()
        val map = AsylumMap()
        val renderer = RaycastRenderer(180, 100) // Small render for fast test

        val bitmap = renderer.render(gameState, map)
        assertNotNull(bitmap)
        assertEquals(180, bitmap.width)
        assertEquals(100, bitmap.height)

        // Flashlight toggle
        gameState.isFlashlightOn = false
        val darkBitmap = renderer.render(gameState, map)
        assertNotNull(darkBitmap)
    }
}
