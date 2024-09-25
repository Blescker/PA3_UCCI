package com.example.pa3.API

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

    object RetrofitClient {
        private const val BASE_URL = "http://10.0.2.2:8000/api/"  // Coloca la URL de tu servidor Django
        val gson = GsonBuilder().setLenient().create()

        val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        val api: ConsumirAPI by lazy {
            retrofit.create(ConsumirAPI::class.java)
        }
    }
