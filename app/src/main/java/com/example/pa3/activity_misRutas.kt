package com.example.pa3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pa3.API.RetrofitClient
import com.example.pa3.API.Ruta
import com.example.pa3.API.RutaGeometry
import com.example.pa3.API.RutasResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class activity_misRutas : AppCompatActivity() {

    private lateinit var listViewRutas: ListView
    private lateinit var adapter: RutaAdapter
    private var rutasList: List<RutaGeometry> = listOf()  // Inicializar como lista vacía

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_rutas)

        listViewRutas = findViewById(R.id.listViewRutas)
        adapter = RutaAdapter(this, rutasList)
        listViewRutas.adapter = adapter

        // Cargar las rutas desde la API
        cargarRutas()
    }

    // Función para cargar las rutas desde el servidor
    private fun cargarRutas() {
        RetrofitClient.api.getRutas().enqueue(object : Callback<RutasResponse> {
            override fun onResponse(call: Call<RutasResponse>, response: Response<RutasResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val rutasResponse = response.body()
                    Log.i("activity_misRutas", "Rutas recibidas: ${rutasResponse?.features}")

                    // Verifica si las características están presentes
                    if (rutasResponse?.features != null && rutasResponse.features.isNotEmpty()) {
                        val adapter = RutaAdapter(this@activity_misRutas, rutasResponse.features)  // Pasa las características (features) al adaptador
                        listViewRutas.adapter = adapter
                    } else {
                        Log.e("activity_misRutas", "No se encontraron rutas.")
                    }
                } else {
                    Log.e("activity_misRutas", "Error al cargar rutas.")
                }
            }

            override fun onFailure(call: Call<RutasResponse>, t: Throwable) {
                Log.e("activity_misRutas", "Error: ${t.message}")
            }
        })
    }
}