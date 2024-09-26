package com.example.pa3.API

data class RutasResponse(
    val type: String,  // Tipo de GeoJSON (FeatureCollection)
    val features: List<RutaGeometry>  // Lista de objetos que contienen geometría y propiedades
)