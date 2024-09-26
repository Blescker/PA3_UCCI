package com.example.pa3

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager

class activity_detalle_ruta : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var polylineAnnotationManager: PolylineAnnotationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_ruta)

        mapView = findViewById(R.id.mapView)

        // Obtener el LINESTRING del intent
        val ubicaciones = intent.getStringExtra("ubicaciones")
        Log.i("DetalleRuta", "Ubicaciones recibidas: $ubicaciones")

        if (ubicaciones != null && ubicaciones.startsWith("SRID=4326;LINESTRING")) {
            val lineString = ubicaciones.removePrefix("SRID=4326;LINESTRING")
            val puntos = convertirLineStringALista(lineString)
            Log.i("DetalleRuta", "Coordenadas procesadas para el mapa: $puntos")

            // Centrar el mapa en la primera ubicación
            if (puntos.isNotEmpty()) {
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(puntos.first())  // Centra en la primera coordenada
                        .zoom(15.0)  // Ajustar el nivel de zoom
                        .build()
                )
            }

            // Cargar el estilo del mapa y dibujar la ruta
            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                dibujarRutaEnMapa(puntos)
            }
        } else {
            Log.e("DetalleRuta", "El campo 'ubicaciones' es nulo o no tiene el formato correcto.")
        }
    }

    // Método para convertir el LINESTRING a una lista de puntos
    private fun convertirLineStringALista(lineString: String): List<Point> {
        // Remover el prefijo y sufijo del LINESTRING
        val coordenadas = lineString.trim()
            .removePrefix("(")
            .removeSuffix(")")
            .split(", ")

        // Convertir cada coordenada en un objeto Point (longitud, latitud)
        return coordenadas.map { coord ->
            val (lng, lat) = coord.split(" ").map { it.toDouble() }
            Point.fromLngLat(lng, lat)
        }
    }
    // Método para dibujar la ruta en el mapa
    private fun dibujarRutaEnMapa(puntos: List<Point>) {
        val annotationApi = mapView?.annotations
        val polylineAnnotationManager = annotationApi?.createPolylineAnnotationManager(mapView)

        val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(puntos)
            .withLineColor("#0000FF")  // Azul para la ruta
            .withLineWidth(2.0)  // Ajustar el grosor de la línea, puedes reducir este valor

        polylineAnnotationManager?.create(polylineAnnotationOptions)
    }
}