package com.example.pa3.API

data class Geometry(
    val type: String,  // Esto debería ser "LineString"
    val coordinates: List<List<Double>>  // Las coordenadas del LINESTRING, una lista de pares [longitud, latitud]
)