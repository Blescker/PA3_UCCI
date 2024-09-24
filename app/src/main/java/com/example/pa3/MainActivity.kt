package com.example.pa3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pa3.API.Estudiante
import com.example.pa3.API.RetrofitClient
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var currentLocation: Point? = null  // Guardar la ubicación actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Mapbox
        mapView = findViewById(R.id.mapView)

        // Acceder a mapboxMap usando la propiedad directamente
        val mapboxMap = mapView.getMapboxMap()

        // Usar el método loadStyle con las constantes de estilo de Mapbox
        mapboxMap.loadStyleUri(getMapStyle()) {
            requestLocationPermission()  // Solicitar permisos de ubicación
        }

        // Mostrar los datos del estudiante
        val studentDataTextView = findViewById<TextView>(R.id.studentDataTextView)
        cargarDatosEstudiante(studentDataTextView)

        // Botón "Ubicarme"
        val btnUbicarme = findViewById<Button>(R.id.btnUbicarme)
        btnUbicarme.setOnClickListener {
            // Verificar si currentLocation tiene valor antes de mover la cámara
            if (currentLocation != null) {
                // Mover la cámara a la ubicación actual
                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(currentLocation)
                        .zoom(14.0)  // Ajusta el nivel de zoom
                        .build()
                )
                Toast.makeText(this, "Centrando en tu ubicación actual", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar un mensaje si la ubicación no está disponible
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                println("currentLocation es null")  // Log para verificar
            }
        }
    }

    // Cambiar el tema del mapa según la hora
    private fun getMapStyle(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (currentHour in 8..18) {
            Style.MAPBOX_STREETS  // Estilo claro para el día
        } else {
            Style.DARK  // Estilo oscuro para la noche
        }
    }

    // Solicitar permisos de ubicación en tiempo de ejecución
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Si ya tiene los permisos, habilita la ubicación
                enableLocationComponent()
            }
            else -> {
                // Solicita el permiso
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Lanzador para solicitar el permiso de ubicación
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si el permiso ha sido otorgado, habilita la ubicación
            enableLocationComponent()
        } else {
            // El permiso no fue otorgado, muestra un mensaje o maneja el caso
            println("Permiso de ubicación denegado")
        }
    }

    // Habilitar el componente de ubicación
    private fun enableLocationComponent() {
        val locationComponentPlugin: LocationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
            locationPuck = LocationPuck2D()  // Configura el marcador de ubicación
        }

        println("Componente de ubicación habilitado")  // Log para verificar que se habilitó

        // Listener para manejar actualizaciones de la posición en tiempo real
        locationComponentPlugin.addOnIndicatorPositionChangedListener { point: Point ->
            // Almacenar la ubicación actual
            currentLocation = point
            println("Ubicación actual: ${point.latitude()}, ${point.longitude()}")  // Verificar que se obtienen las coordenadas
        }
    }

    // Cargar los datos del estudiante desde la API
    private fun cargarDatosEstudiante(studentDataTextView: TextView) {
        RetrofitClient.api.getEstudiantes().enqueue(object : Callback<List<Estudiante>> {
            override fun onResponse(call: Call<List<Estudiante>>, response: Response<List<Estudiante>>) {
                if (response.isSuccessful && response.body() != null) {
                    val estudiante = response.body()!!.firstOrNull() // Tomamos el primer estudiante
                    estudiante?.let {
                        val data = "${it.nombres} ${it.apellidos}\nCódigo: ${it.codigo}"
                        studentDataTextView.text = data
                    }
                }
            }

            override fun onFailure(call: Call<List<Estudiante>>, t: Throwable) {
                studentDataTextView.text = "Error al cargar los datos"
            }
        })
    }
}
