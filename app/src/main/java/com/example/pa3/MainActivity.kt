package com.example.pa3

import android.Manifest
import android.content.Intent
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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var currentLocation: Point? = null  // Guardar la ubicación actual
    private lateinit var pointAnnotationManager: PointAnnotationManager  // Para manejar los markers


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Mapbox
        mapView = findViewById(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            enableLocationComponent()
        }

        pointAnnotationManager = mapView.annotations.createPointAnnotationManager(mapView)

        // Acceder a mapboxMap usando la propiedad directamente
        val mapboxMap = mapView.getMapboxMap()

        // Usar el método loadStyle con las constantes de estilo de Mapbox
        mapboxMap.loadStyleUri(getMapStyle()) {
            requestLocationPermission()  // Solicitar permisos de ubicación
        }
        val btnIrSeguimiento = findViewById<Button>(R.id.btnIrSeguimiento)

        // Al hacer clic, se lanza la Activity Seguimiento
        btnIrSeguimiento.setOnClickListener {
            // Verificar si la ubicación actual está disponible
            if (currentLocation != null) {
                // Crear un Intent para cambiar a SeguimientoActivity
                val intent = Intent(this, activity_seguimiento::class.java)
                intent.putExtra("latitud", currentLocation!!.latitude())  // Guardar latitud
                intent.putExtra("longitud", currentLocation!!.longitude())  // Guardar longitud
                startActivity(intent)
            } else {
                // Manejar el caso en el que la ubicación aún no esté disponible
                println("Ubicación no disponible")
            }
        }

        // Mostrar los datos del estudiante
        val studentDataTextView = findViewById<TextView>(R.id.studentDataTextView)
        cargarDatosEstudiante(studentDataTextView)

        // Botón "Ubicarme"
        val btnUbicarme = findViewById<Button>(R.id.btnUbicarme)
        btnUbicarme.setOnClickListener {
            // Verificar si currentLocation tiene valor antes de mover la cámara
            if (currentLocation != null) {
                // Mover la cámara a la ubicación actual con flyTo
                mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(currentLocation)  // Centrar la cámara en la ubicación actual
                        .zoom(15.0)  // Ajusta el nivel de zoom para acercar la vista
                        .build()
                )
                // Colocar un marker en la ubicación actual
                addMarkerAtLocation(currentLocation!!)
                val intent = Intent(this, activity_seguimiento::class.java)
                intent.putExtra("latitud", currentLocation!!.latitude())
                intent.putExtra("longitud", currentLocation!!.longitude())
                startActivity(intent)

                Toast.makeText(this, "Centrando en tu ubicación actual", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar un mensaje si la ubicación no está disponible
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
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
    // Agregar un marker en la ubicación actual
    private fun addMarkerAtLocation(location: Point) {
        // Crear el marker en la ubicación dada
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.red_marker)  // Asegúrate de tener un ícono llamado marker_icon
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 64, 64, false)  // Cambia los valores 64x64 según el tamaño que desees

        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(location)  // Establece la ubicación del marker
            .withIconImage(scaledBitmap)  // Puedes personalizar el icono si lo deseas

        // Añadir el marker
        pointAnnotationManager.create(pointAnnotationOptions)
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
