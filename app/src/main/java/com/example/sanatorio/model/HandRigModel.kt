package com.example.sanatorio.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Representación de un hueso individual modificable en tiempo de ejecución
 * extraído del asset 3D 'investigator_hands' (glTF 2.0 / OBJ con armadura).
 */
data class BoneJoint(
    val id: Int,
    val name: String,
    val displayName: String,
    val role: String,
    val defaultX: Float = 0f,
    val defaultY: Float = 0f,
    val defaultZ: Float = 0f
) {
    var angleX by mutableFloatStateOf(defaultX)
    var angleY by mutableFloatStateOf(defaultY)
    var angleZ by mutableFloatStateOf(defaultZ)
}

/**
 * Poses preconfiguradas para manipular la mano en tiempo de ejecución
 */
data class HandPosePreset(
    val id: String,
    val title: String,
    val description: String,
    val boneRotations: Map<String, Triple<Float, Float, Float>>
)

class HandRigManager {
    val assetPath = "models/investigator_hands/"
    val gltfFile = "investigator_hands.gltf"
    val objFile = "investigator_hands.obj"
    val rigFile = "rig_info.json"
    val license = "CC0 1.0 Universal (Dominio Público)"

    var currentPoseName by mutableStateOf("Anim_Flashlight_Grip")

    val bones = mutableStateListOf(
        BoneJoint(1, "Bone_Forearm_Wrist", "Muñeca / Antebrazo", "Orientación base del antebrazo hacia la linterna", 12f, -5f, 0f),
        BoneJoint(2, "Bone_Hand_Palm", "Palma de la Mano", "Cuerpo central de la mano del investigador", 8f, 0f, -4f),
        BoneJoint(3, "Bone_Thumb_Metacarpal", "Metacarpo Pulgar", "Base del pulgar apoyada en el botón de encendido", 25f, -15f, 10f),
        BoneJoint(4, "Bone_Thumb_Phalanx", "Falange Pulgar", "Punta del pulgar presionando el switch mecánico", 30f, 0f, 5f),
        BoneJoint(5, "Bone_Index_Proximal", "Índice Proximal", "Primera articulación del dedo índice rodeando el cilindro", 45f, 5f, -2f),
        BoneJoint(6, "Bone_Index_Distal", "Índice Distal", "Cierre del índice envolviendo el mango metálico", 40f, 0f, 0f),
        BoneJoint(7, "Bone_Middle_Finger", "Dedo Medio", "Agarre de soporte central en el cuerpo de la linterna", 50f, 2f, -3f),
        BoneJoint(8, "Bone_Ring_Pinky_Group", "Anular y Meñique", "Sujeción de seguridad inferior", 55f, -4f, -5f)
    )

    val presets = listOf(
        HandPosePreset(
            id = "Anim_Flashlight_Grip",
            title = "Agarre de Linterna",
            description = "Dedos cerrados firmemente alrededor del cilindro de aluminio, pulgar en switch.",
            boneRotations = mapOf(
                "Bone_Forearm_Wrist" to Triple(12f, -5f, 0f),
                "Bone_Hand_Palm" to Triple(8f, 0f, -4f),
                "Bone_Thumb_Metacarpal" to Triple(25f, -15f, 10f),
                "Bone_Thumb_Phalanx" to Triple(30f, 0f, 5f),
                "Bone_Index_Proximal" to Triple(45f, 5f, -2f),
                "Bone_Index_Distal" to Triple(40f, 0f, 0f),
                "Bone_Middle_Finger" to Triple(50f, 2f, -3f),
                "Bone_Ring_Pinky_Group" to Triple(55f, -4f, -5f)
            )
        ),
        HandPosePreset(
            id = "Anim_Hand_Inspect",
            title = "Extensión de Inspección",
            description = "Mano abierta proyectándose hacia adelante para examinar evidencias o puertas.",
            boneRotations = mapOf(
                "Bone_Forearm_Wrist" to Triple(5f, 0f, 10f),
                "Bone_Hand_Palm" to Triple(2f, 0f, 0f),
                "Bone_Thumb_Metacarpal" to Triple(10f, -5f, 2f),
                "Bone_Thumb_Phalanx" to Triple(5f, 0f, 0f),
                "Bone_Index_Proximal" to Triple(12f, 2f, 0f),
                "Bone_Index_Distal" to Triple(8f, 0f, 0f),
                "Bone_Middle_Finger" to Triple(10f, 0f, 0f),
                "Bone_Ring_Pinky_Group" to Triple(15f, 0f, 0f)
            )
        ),
        HandPosePreset(
            id = "Anim_Tremble_Insanity",
            title = "Temblor de Pánico (Terror)",
            description = "Mano en espasmo y tensión con dedos crispados por pérdida de cordura.",
            boneRotations = mapOf(
                "Bone_Forearm_Wrist" to Triple(18f, -12f, 8f),
                "Bone_Hand_Palm" to Triple(14f, -4f, -8f),
                "Bone_Thumb_Metacarpal" to Triple(35f, -20f, 15f),
                "Bone_Thumb_Phalanx" to Triple(40f, -5f, 10f),
                "Bone_Index_Proximal" to Triple(60f, 8f, -6f),
                "Bone_Index_Distal" to Triple(55f, 0f, 2f),
                "Bone_Middle_Finger" to Triple(65f, 5f, -5f),
                "Bone_Ring_Pinky_Group" to Triple(70f, -8f, -10f)
            )
        ),
        HandPosePreset(
            id = "Anim_Relaxed_Rest",
            title = "Descanso Relajado",
            description = "Articulaciones en reposo con curvatura anatómica neutra.",
            boneRotations = mapOf(
                "Bone_Forearm_Wrist" to Triple(0f, 0f, 0f),
                "Bone_Hand_Palm" to Triple(0f, 0f, 0f),
                "Bone_Thumb_Metacarpal" to Triple(15f, -10f, 0f),
                "Bone_Thumb_Phalanx" to Triple(10f, 0f, 0f),
                "Bone_Index_Proximal" to Triple(20f, 0f, 0f),
                "Bone_Index_Distal" to Triple(15f, 0f, 0f),
                "Bone_Middle_Finger" to Triple(22f, 0f, 0f),
                "Bone_Ring_Pinky_Group" to Triple(25f, 0f, 0f)
            )
        )
    )

    fun applyPreset(presetId: String) {
        val preset = presets.find { it.id == presetId } ?: return
        currentPoseName = preset.id
        preset.boneRotations.forEach { (boneName, rot) ->
            bones.find { it.name == boneName }?.let { bone ->
                bone.angleX = rot.first
                bone.angleY = rot.second
                bone.angleZ = rot.third
            }
        }
    }

    fun resetToDefaults() {
        applyPreset("Anim_Flashlight_Grip")
    }
}
