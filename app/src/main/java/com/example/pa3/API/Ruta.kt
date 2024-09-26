package com.example.pa3.API

data class Ruta(
    val id: Int? = null,
    val nombre_ubicacion: String,
    val estudiante: Int,  // Cambia estudiante_id a estudiante
    val fecha_inicio: String,
    val fecha_fin: String? = null,
    val ubicaciones: String,  // Lista de puntos de latitud y longitud
    val completado: Boolean = false
)
