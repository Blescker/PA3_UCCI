package com.example.pa3.API

data class Ruta(
    val id: Int? = null,
    val estudiante: Estudiante,
    val nombre_ruta: String,
    val fecha_inicio: String,  // El formato ISO de fecha y hora
    val fecha_fin: String?,    // Puede ser nulo
    val ubicaciones: List<Ubicacion>
)
