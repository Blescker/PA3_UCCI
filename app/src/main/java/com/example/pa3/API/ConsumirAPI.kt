package com.example.pa3.API

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

    interface ConsumirAPI {
        // Obtener todos los estudiantes
        @GET("estudiantes/")
        fun getEstudiantes(): Call<List<Estudiante>>

        @POST("rutas/")
        fun guardarRuta(@Body ruta: Ruta): Call<Ruta>

    }
