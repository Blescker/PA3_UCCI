package com.example.pa3

import android.content.Intent
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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import java.util.Calendar

class activity_seguimiento : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var btnIniciarSeguimiento: Button
    private lateinit var btnDetenerSeguimiento: Button
    private lateinit var nombreSeguimiento: EditText
    private lateinit var tvEstadoSeguimiento: TextView
    private lateinit var btnMisRutas: Button
    private var ubicaciones = mutableListOf<List<Double>>()  // Para guardar las ubicaciones
    private var isTracking = false
    private var handler: Handler? = null
    private var simulationIndex = 0
    private var currentLocation: Point? = null
    private var initialLocation: Point? = null  // Para almacenar la ubicación inicial pasada
    private lateinit var pointAnnotationManager: PointAnnotationManager  // Para manejar los markers
    private val simulatedLocations = mutableListOf<Point>()  // Lista de ubicaciones simuladas
    private lateinit var currentMarker: PointAnnotation  // Mantener referencia al marcador actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seguimiento)

        mapView = findViewById(R.id.mapView)
        btnIniciarSeguimiento = findViewById(R.id.btnIniciarSeguimiento)
        btnDetenerSeguimiento = findViewById(R.id.btnDetenerSeguimiento)
        nombreSeguimiento = findViewById(R.id.nombreSeguimiento)
        tvEstadoSeguimiento = findViewById(R.id.tvEstadoSeguimiento)
        btnMisRutas = findViewById(R.id.btnMisRutas)

        // Obtener los extras del Intent
        val latitud = intent.getDoubleExtra("latitud", -12.075433211901995)
        val longitud = intent.getDoubleExtra("longitud", -75.20929064478399)

        // Iniciar el seguimiento al hacer clic en el botón
        btnIniciarSeguimiento.setOnClickListener {
            if (!isTracking && nombreSeguimiento.text.isNotEmpty()) {
                iniciarSeguimiento()
            }
        }

        btnMisRutas.setOnClickListener{
            val intent = Intent(this, activity_misRutas::class.java)
            startActivity(intent)
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
        mapView.getMapboxMap().loadStyleUri(getMapStyle()) {
            // Si hay una ubicación inicial, centrar el mapa en esa ubicación
            if (initialLocation != null) {
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(initialLocation)  // Centrar el mapa en la ubicación inicial
                        .zoom(16.0)  // Nivel de zoom ajustado
                        .build()
                )
                // Inicializar el PointAnnotationManager
                pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

                // Crear el marcador inicial en la ubicación inicial
                currentMarker = addMarkerAtLocation(initialLocation!!)
            }

            // Inicializar las ubicaciones simuladas
            initializeSimulatedLocations(latitud, longitud)
        }
    }

    // Método para agregar un marcador en la ubicación inicial
    private fun addMarkerAtLocation(location: Point): PointAnnotation {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 52, 52, false)

        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location)
            .withIconImage(scaledBitmap)

        return pointAnnotationManager.create(pointAnnotationOptions)  // Retorna el marcador creado
    }

    // Inicializar la lista de ubicaciones simuladas
    private fun initializeSimulatedLocations(latitudInicial: Double, longitudInicial: Double) {
        val steps = 10  // Número de pasos a simular
        val incrementoLatitud = 0.00005  // Incremento constante en la latitud para moverse al norte
        val incrementoLongitud = 0.0001  // Incremento constante en la longitud para moverse al este

        // Generar las ubicaciones simuladas paso a paso
        for (i in 0..steps) {
            val nuevaLatitud = latitudInicial + (i * incrementoLatitud)
            val nuevaLongitud = longitudInicial + (i * incrementoLongitud)
            simulatedLocations.add(Point.fromLngLat(nuevaLongitud, nuevaLatitud))
        }
    }
    // Función para obtener el estilo del mapa según la hora del día
    private fun getMapStyle(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (currentHour in 8..18) {
            Style.MAPBOX_STREETS  // Estilo claro para el día (8am - 6pm)
        } else {
            Style.DARK  // Estilo oscuro para la noche
        }
    }
    private fun iniciarSeguimiento() {
        isTracking = true
        tvEstadoSeguimiento.text = "Estado: En seguimiento"
        btnIniciarSeguimiento.visibility = Button.GONE
        btnDetenerSeguimiento.visibility = Button.VISIBLE

        // Inicializar el handler para mover el marcador periódicamente
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(object : Runnable {
            override fun run() {
                if (simulationIndex < simulatedLocations.size && isTracking) {
                    val newLocation = simulatedLocations[simulationIndex]

                    // Mover la cámara a la nueva ubicación
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(newLocation)
                            .zoom(16.0) // Ajusta el zoom según sea necesario
                            .build()
                    )

                    // Crear un nuevo marcador en la nueva ubicación, sin reutilizar nada
                    val newMarker = addNewMarkerAtLocation(newLocation)

                    // Agregar la nueva ubicación a la lista de ubicaciones
                    ubicaciones.add(listOf(newLocation.latitude(), newLocation.longitude()))

                    simulationIndex++
                    handler?.postDelayed(this, 1000)  // Continuar la simulación cada segundo
                }
            }
        }, 1000)
    }

    // Método para agregar un nuevo marcador en la ubicación actual
    private fun addNewMarkerAtLocation(location: Point): PointAnnotation {
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 52, 52, false)

        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location)
            .withIconImage(scaledBitmap)

        // Aquí se crea y retorna un nuevo marcador en cada llamado
        return pointAnnotationManager.create(pointAnnotationOptions)
    }

    // Llamar a este método cuando el seguimiento se detenga para eliminar el marcador
    private fun detenerSeguimiento() {
        isTracking = false
        tvEstadoSeguimiento.text = "Estado: Seguimiento detenido"
        btnIniciarSeguimiento.visibility = Button.VISIBLE
        btnDetenerSeguimiento.visibility = Button.GONE

        // Al finalizar el seguimiento, eliminar el marcador actual
        if (::currentMarker.isInitialized) {
            pointAnnotationManager.delete(currentMarker)
        }

        // Lógica para guardar las ubicaciones en el servidor
        guardarRutaEnServidor(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()))
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
