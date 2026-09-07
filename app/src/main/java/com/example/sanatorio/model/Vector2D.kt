package com.example.sanatorio.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Double, val y: Double) {
    operator fun plus(other: Vector2D): Vector2D = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Double): Vector2D = Vector2D(x * scalar, y * scalar)
    
    fun length(): Double = sqrt(x * x + y * y)
    
    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001) Vector2D(x / len, y / len) else Vector2D(0.0, 0.0)
    }

    fun rotated(angleRad: Double): Vector2D {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector2D(x * cosA - y * sinA, x * sinA + y * cosA)
    }

    fun distanceTo(other: Vector2D): Double {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }
}
