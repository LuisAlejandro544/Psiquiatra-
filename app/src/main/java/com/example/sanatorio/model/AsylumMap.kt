package com.example.sanatorio.model

class AsylumMap {
    companion object {
        const val WIDTH = 20
        const val HEIGHT = 20

        // Tile identifiers
        const val EMPTY = 0
        const val WALL_TILES = 1        // Greenish decayed hospital tiles
        const val WALL_PADDED = 2       // Padded cell diamond cushioning
        const val WALL_BARS = 3         // Iron cell security bars
        const val WALL_DOOR = 4         // Weathered asylum door with observation slit
        const val WALL_BLOOD = 5        // Distressed wall with crimson claw/stain marks
        const val WALL_BRICK = 6        // Broken plaster wall revealing old bricks
    }

    // 20x20 grid representing the psychiatric facility floor
    val grid: Array<IntArray> = arrayOf(
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 4, 1, 1, 0, 0, 0, 0, 1, 1, 4, 1, 1, 1, 4, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 4, 1, 1, 0, 0, 0, 0, 1, 1, 4, 1, 1, 1, 4, 1, 1, 1, 1),
        intArrayOf(1, 2, 2, 2, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 0, 2, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 2, 2, 2, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 4, 1, 1, 0, 0, 0, 0, 1, 1, 4, 1, 1, 1, 3, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1),
        intArrayOf(1, 1, 4, 1, 1, 0, 0, 0, 0, 1, 1, 4, 1, 1, 1, 3, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 4, 0, 0, 0, 0, 4, 0, 0, 0, 4, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1),
        intArrayOf(1, 5, 5, 5, 1, 6, 6, 6, 6, 1, 5, 5, 5, 1, 6, 6, 6, 6, 6, 1),
        intArrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )

    // Initial decorations list placed accurately across rooms and corridors
    val decorations: MutableList<Decoration> = mutableListOf(
        // Reception & Main Corridor
        Decoration(
            id = "lamp_corridor_1",
            type = DecorationType.FLICKERING_LAMP,
            x = 6.5, y = 5.5,
            name = "Tubo Fluorescente Parpadeante",
            description = "Un fluorescente industrial que emite un zumbido eléctrico errático.",
            isLightEmitter = true,
            canInspect = true,
            loreSnippet = "El zumbido a 60Hz reverbera en el pasillo. La cubierta plástica está manchada de tizne."
        ),
        Decoration(
            id = "wheelchair_1",
            type = DecorationType.WHEELCHAIR,
            x = 7.5, y = 6.2,
            name = "Silla de Ruedas Enmohecida",
            description = "Una silla ortopédica con correas de cuero resecas y ruedas chirriantes.",
            loreSnippet = "Las correas de sujeción fueron forzadas desde dentro. Hay marcas de uñas profundas en el reposabrazos derecho.",
            canInspect = true
        ),
        Decoration(
            id = "patient_file_1",
            type = DecorationType.PATIENT_FILE,
            x = 2.5, y = 2.2,
            name = "Expediente: Paciente #209",
            description = "Una carpeta clínica de cartón amarillento manchada por la humedad.",
            loreSnippet = "'Paciente #209 - Diagnóstico: Histeria persecutoria. Insiste en que cuando las luces se apagan, las paredes se abren y algo susurra su verdadero nombre.'",
            canInspect = true
        ),
        // Solitary Padded Cell
        Decoration(
            id = "blood_padded",
            type = DecorationType.BLOOD_MARK,
            x = 2.5, y = 9.5,
            name = "Inscripción en la Celda Acolchada",
            description = "Palabras trazadas con sangre reseca sobre el acolchado de cuero desgarrado.",
            loreSnippet = "'NO DEJES QUE LA LUZ SE APAGUE. EN LA OSCURIDAD ÉL PUEDE TOCARTE.'",
            canInspect = true
        ),
        // Electroshock / Treatment room
        Decoration(
            id = "gurney_1",
            type = DecorationType.GURNEY,
            x = 11.5, y = 9.5,
            name = "Camilla de Terapia Electroconvulsiva",
            description = "Camilla metálica de hospital con electrodos colgando y correas reforzadas en cabeza y muñecas.",
            loreSnippet = "La máquina de electroshock indica una descarga máxima aplicada el 14 de Octubre de 1984, la noche del desalojo forzado.",
            canInspect = true
        ),
        Decoration(
            id = "lamp_electroshock",
            type = DecorationType.FLICKERING_LAMP,
            x = 11.5, y = 8.5,
            name = "Lámpara de Quirófano Parpadeante",
            description = "Una lámpara de examen que oscila suavemente como si alguien la hubiera rozado.",
            isLightEmitter = true,
            canInspect = true,
            loreSnippet = "La bombilla parpadea con violencia cada pocos segundos, sumiendo la sala en tinieblas intermitentes."
        ),
        Decoration(
            id = "medicine_cart_1",
            type = DecorationType.MEDICINE_CART,
            x = 12.5, y = 10.2,
            name = "Carro de Sedantes",
            description = "Bandeja de acero con frascos rotos de Clorpromazina y jeringas de vidrio.",
            loreSnippet = "La mayoría de las dosis de sedantes fueron extraídas con prisa, como si alguien hubiera intentado dormir a todos de golpe.",
            canInspect = true
        ),
        // Corridor B
        Decoration(
            id = "iv_stand_1",
            type = DecorationType.IV_STAND,
            x = 16.5, y = 5.8,
            name = "Soporte de Suero Inclinado",
            description = "Un gotero oxidado con una bolsa que contiene un líquido oscuro y espeso.",
            loreSnippet = "El catéter tiene restos de sangre coagulada. Parece haber sido arrancado bruscamente del paciente.",
            canInspect = true
        ),
        // Doctor's Office
        Decoration(
            id = "doctor_desk",
            type = DecorationType.OLD_DESK,
            x = 2.5, y = 16.5,
            name = "Escritorio del Dr. Valdés",
            description = "Un escritorio de nogal desvencijado con una máquina de escribir Smith Corona y expedientes.",
            loreSnippet = "'Diario del Director (última entrada): La junta exige silencio sobre los ruidos subterráneos. Ya no son alucinaciones colectivas. El personal se niega a entrar al pabellón de aislamiento sin antorchas.'",
            canInspect = true
        ),
        Decoration(
            id = "lamp_office",
            type = DecorationType.FLICKERING_LAMP,
            x = 2.5, y = 15.5,
            name = "Flexo de Sobremesa Verde",
            description = "Lámpara de banquero con pantalla verde que zumba y emite chispas diminutas.",
            isLightEmitter = true,
            canInspect = true,
            loreSnippet = "El interruptor está atascado en 'encendido', pero la corriente fluctúa constantemente."
        ),
        // Isolation Block (behind bars)
        Decoration(
            id = "patient_file_2",
            type = DecorationType.PATIENT_FILE,
            x = 17.5, y = 12.5,
            name = "Expediente Clasificado #001",
            description = "Expediente sellado en carpeta negra con cinta de precaución.",
            loreSnippet = "'Sujeto Alfa: Primer internado de la institución. No posee registro de identidad civil. El informe forense concluye que no posee pulso cardíaco detectable a oscuras.'",
            canInspect = true
        )
    )

    fun isWall(x: Double, y: Double): Boolean {
        val mapX = x.toInt()
        val mapY = y.toInt()
        if (mapX < 0 || mapX >= WIDTH || mapY < 0 || mapY >= HEIGHT) return true
        val tile = grid[mapY][mapX]
        return tile != EMPTY
    }

    fun getTile(mapX: Int, mapY: Int): Int {
        if (mapX < 0 || mapX >= WIDTH || mapY < 0 || mapY >= HEIGHT) return WALL_TILES
        return grid[mapY][mapX]
    }

    fun getRoomName(x: Double, y: Double): String {
        val mapX = x.toInt()
        val mapY = y.toInt()
        return when {
            mapX in 1..4 && mapY in 1..4 -> "Sala de Admisión #101"
            mapX in 1..4 && mapY in 8..11 -> "Celda de Aislamiento Acolchada"
            mapX in 10..13 && mapY in 8..11 -> "Terapia Electroconvulsiva"
            mapX in 1..4 && mapY in 15..18 -> "Despacho del Dr. Valdés"
            mapX in 14..18 && mapY in 11..15 -> "Pabellón de Contención Máxima"
            mapX in 14..18 && mapY in 1..4 -> "Almacén de Farmacia"
            mapY in 5..7 -> "Pasillo Central - Ala Norte"
            mapY in 12..14 -> "Pasillo Secundario - Ala Sur"
            else -> "Pasillo de Vigilancia"
        }
    }
}
