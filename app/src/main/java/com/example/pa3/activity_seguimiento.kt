package com.example.pa3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pa3.API.Ruta
import com.example.pa3.API.RetrofitClient
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.gson.Gson

class activity_seguimiento : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var btnIniciarSeguimiento: Button
    private lateinit var btnDetenerSeguimiento: Button
    private lateinit var nombreSeguimiento: EditText
    private lateinit var tvEstadoSeguimiento: TextView
    private var ubicaciones = mutableListOf<List<Double>>()  // Para guardar las ubicaciones
    private var isTracking = false
    private var handler: Handler? = null
    private var simulationIndex = 0
    private var currentLocation: Point? = null
    private var initialLocation: Point? = null  // Para almacenar la ubicación inicial pasada
    private lateinit var pointAnnotationManager: PointAnnotationManager  // Para manejar los markers
    private val simulatedLocations = mutableListOf<Point>()  // Lista de ubicaciones simuladas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        mapView = findViewById(R.id.mapView)
        btnIniciarSeguimiento = findViewById(R.id.btnIniciarSeguimiento)
        btnDetenerSeguimiento = findViewById(R.id.btnDetenerSeguimiento)
        nombreSeguimiento = findViewById(R.id.nombreSeguimiento)
        tvEstadoSeguimiento = findViewById(R.id.tvEstadoSeguimiento)

        // Obtener los extras del Intent
        val latitud = intent.getDoubleExtra("latitud", -12.075433211901995)
        val longitud = intent.getDoubleExtra("longitud", -75.20929064478399)

        // Iniciar el seguimiento al hacer clic en el botón
        btnIniciarSeguimiento.setOnClickListener {
            if (!isTracking && nombreSeguimiento.text.isNotEmpty()) {
                iniciarSeguimiento()
            }
        }

        // Detener el seguimiento
        btnDetenerSeguimiento.setOnClickListener {
            if (isTracking) {
                detenerSeguimiento()
            }
        }

        // Verificar si se pasó una ubicación válida
        if (latitud != 0.0 && longitud != 0.0) {
            initialLocation = Point.fromLngLat(longitud, latitud)
        }

        // Inicializar Mapbox
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            // Si hay una ubicación inicial, centrar el mapa en esa ubicación
            if (initialLocation != null) {
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(initialLocation)  // Centrar el mapa en la ubicación inicial
                        .zoom(15.0)  // Nivel de zoom ajustado
                        .build()
                )
                // Inicializar el PointAnnotationManager
                pointAnnotationManager = mapView.annotations.createPointAnnotationManager(mapView)

                // Agregar el marcador en la ubicación inicial
                addMarkerAtLocation(initialLocation!!)
            }

            // Inicializar las ubicaciones simuladas
            initializeSimulatedLocations(latitud, longitud)
        }
    }

    // Método para agregar un marcador en la ubicación inicial
    private fun addMarkerAtLocation(location: Point) {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 64, 64, false)

        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location)
            .withIconImage(scaledBitmap)

        pointAnnotationManager.create(pointAnnotationOptions)
    }

    // Inicializar la lista de ubicaciones simuladas
    private fun initializeSimulatedLocations(latitud: Double, longitud: Double) {
        simulatedLocations.add(Point.fromLngLat(longitud, latitud))  // Posición inicial
        simulatedLocations.add(Point.fromLngLat(longitud + 0.0001, latitud))  // Movimiento hacia el este
        simulatedLocations.add(Point.fromLngLat(longitud + 0.0002, latitud + 0.0001))  // Movimiento noreste
        simulatedLocations.add(Point.fromLngLat(longitud + 0.0003, latitud + 0.0002))  // Más movimiento noreste
        simulatedLocations.add(Point.fromLngLat(longitud + 0.0004, latitud + 0.0003))  // Y más movimiento
    }

    private fun iniciarSeguimiento() {
        isTracking = true
        tvEstadoSeguimiento.text = "Estado: En seguimiento"
        btnIniciarSeguimiento.visibility = Button.GONE
        btnDetenerSeguimiento.visibility = Button.VISIBLE

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(object : Runnable {
            override fun run() {
                if (simulationIndex < simulatedLocations.size) {
                    val newLocation = simulatedLocations[simulationIndex]

                    // Agregar la ubicación simulada a la lista de ubicaciones
                    ubicaciones.add(listOf(newLocation.latitude(), newLocation.longitude()))

                    // Mover la cámara a la nueva ubicación
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(newLocation)
                            .build()
                    )

                    // Remover el marcador anterior y agregar uno nuevo
                    pointAnnotationManager.deleteAll()
                    addMarkerAtLocation(newLocation)

                    simulationIndex++
                    if (isTracking) {
                        handler?.postDelayed(this, 1000)
                    }
                }
            }
        }, 1000)
    }

    private fun detenerSeguimiento() {
        isTracking = false
        tvEstadoSeguimiento.text = "Estado: Seguimiento detenido"
        btnIniciarSeguimiento.visibility = Button.VISIBLE
        btnDetenerSeguimiento.visibility = Button.GONE

        val fechaFin = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        // Enviar la ruta al servidor
        guardarRutaEnServidor(fechaFin)
    }

    private fun guardarRutaEnServidor(fechaFin:String?) {
        val nombre = nombreSeguimiento.text.toString()
        val estudianteId = 1  // Este es el ID correcto del estudiante
        val fechaInicio = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        // Convertir la lista de ubicaciones a formato JSON
        val gson = Gson()
        val ubicacionesJson = gson.toJson(ubicaciones)

        // Crear el objeto Ruta con el campo estudiante en lugar de estudiante_id
        val ruta = Ruta(
            nombre_ubicacion = nombre,
            estudiante = estudianteId,  // Cambia estudiante_id a estudiante
            fecha_inicio = fechaInicio,
            fecha_fin = fechaFin,
            ubicaciones = ubicacionesJson,
            completado = true
        )

        // Imprimir el objeto ruta para depuración
        println("Datos enviados a la API: $ruta")

        // Llamar a la API para guardar la ruta
        RetrofitClient.api.guardarRuta(ruta).enqueue(object : Callback<Ruta> {
            override fun onResponse(call: Call<Ruta>, response: Response<Ruta>) {
                if (response.isSuccessful) {
                    tvEstadoSeguimiento.text = "Seguimiento guardado en el servidor"
                } else {
                    val errorBody = response.errorBody()?.string()
                    tvEstadoSeguimiento.text = "Error al guardar la ruta: $errorBody"
                    println("Error al guardar la ruta: $errorBody")
                }
            }

            override fun onFailure(call: Call<Ruta>, t: Throwable) {
                tvEstadoSeguimiento.text = "Error: ${t.message}"
            }
        })
    }

}
