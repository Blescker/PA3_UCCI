package com.example.pa3.API

data class RutaGeometry(
    val type: String,  // El tipo de GeoJSON, debería ser "Feature"
    val geometry: String,  // El campo geometry es un LINESTRING con SRID, que es un String
    val properties: Ruta  // Este contiene los detalles de la ruta
)