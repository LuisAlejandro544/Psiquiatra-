package com.example.sanatorio.model

data class GameState(
    var playerX: Double = 6.0,
    var playerY: Double = 6.0,
    var dirX: Double = 1.0,
    var dirY: Double = 0.0,
    var planeX: Double = 0.0,
    var planeY: Double = 0.75, // FOV around 74 degrees
    var pitch: Float = 0f,
    var headBob: Float = 0f,
    
    // Flashlight
    var isFlashlightOn: Boolean = true,
    var flashlightBattery: Float = 98f,
    var flashlightFlickerFactor: Float = 1.0f,
    
    // Ambient & Fluorescent lighting
    var overheadLightFlicker: Float = 0.8f,
    var isBlackout: Boolean = false,
    
    // Investigation & Sanity
    var currentRoom: String = "Pasillo Central - Ala Norte",
    var targetDecoration: Decoration? = null,
    var activeInspection: Decoration? = null,
    var isNotebookOpen: Boolean = false,
    var sanity: Float = 100f,
    var isSoundEnabled: Boolean = true,
    var discoveredLoreCount: Int = 0,
    var totalLoreCount: Int = 5
)
