package com.example.pa3.API

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

    interface ConsumirAPI {
        // Obtener todos los estudiantes
        @GET("estudiantes/")
        fun getEstudiantes(): Call<List<Estudiante>>

        // Obtener todas las rutas
        @GET("rutas/")
        fun getRutas(): Call<List<Ruta>>

        // Obtener una ruta específica por su ID
        @GET("rutas/{id}/")
        fun getRutaById(@Path("id") id: Int): Call<Ruta>
    }
