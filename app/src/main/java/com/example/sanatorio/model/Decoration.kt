package com.example.sanatorio.model

enum class DecorationType {
    WHEELCHAIR,
    GURNEY,
    IV_STAND,
    PATIENT_FILE,
    MEDICINE_CART,
    BLOOD_MARK,
    FLICKERING_LAMP,
    OLD_DESK
}

data class Decoration(
    val id: String,
    val type: DecorationType,
    var x: Double,
    var y: Double,
    val name: String,
    val description: String,
    val loreSnippet: String? = null,
    val canInspect: Boolean = true,
    var isInspected: Boolean = false,
    val isLightEmitter: Boolean = false
)
