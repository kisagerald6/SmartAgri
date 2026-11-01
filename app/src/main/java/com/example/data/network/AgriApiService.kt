package com.example.data.network

import com.example.data.model.AgriStatus
import com.example.data.model.TestAlertResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.concurrent.TimeUnit

interface AgriApiService {
  @GET("/api/status")
  suspend fun getStatus(): AgriStatus

  @GET("/api/pump")
  suspend fun setPumpState(@Query("state") state: String): AgriStatus

  @GET("/api/water")
  suspend fun triggerWater(@Query("sec") seconds: Int): AgriStatus

  @GET("/api/resume")
  suspend fun resumeAuto(): AgriStatus

  @GET("/api/mode")
  suspend fun setMode(@Query("mode") mode: String): AgriStatus

  @GET("/api/config")
  suspend fun updateConfig(@QueryMap params: Map<String, String>): AgriStatus

  @GET("/api/clear")
  suspend fun clearFault(): AgriStatus

  @GET("/api/resetflow")
  suspend fun resetFlow(): AgriStatus

  @GET("/api/calibrate")
  suspend fun calibrate(@Query("point") point: String): AgriStatus

  @GET("/api/testalert")
  suspend fun testAlert(): TestAlertResponse

  @GET("/api/demo")
  suspend fun triggerDemoAction(@Query("x") action: String): AgriStatus

  companion object {
    private val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    fun create(host: String): AgriApiService {
      val cleanHost = host.trim()
        .removePrefix("http://")
        .removePrefix("https://")
        .substringBefore('/')

      val baseUrl = if (cleanHost.isNotEmpty()) "http://$cleanHost/" else "http://127.0.0.1/"

      val client = OkHttpClient.Builder()
        .connectTimeout(4500, TimeUnit.MILLISECONDS)
        .readTimeout(4500, TimeUnit.MILLISECONDS)
        .writeTimeout(4500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

      return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AgriApiService::class.java)
    }
  }
}
