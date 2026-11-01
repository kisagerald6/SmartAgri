package com.example.data.model

data class ZoneEnvironmentData(
  val zoneId: String,
  val name: String,
  val cropType: String,
  val areaSqMeters: Double,
  val temperatureC: Double,
  val humidityPct: Double,
  val lightLux: Double,
  val lightDliMol: Double,
  val soilMoisturePct: Double,
  val nutrientsPpm: Double,
  val tankLevelPct: Double,
  val flowRateLMin: Double,
  val isIrrigating: Boolean,
  val hostAddress: String = "192.168.1.50",
  val isDemo: Boolean = true
)
