package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class AlertLevel {
  CRITICAL,
  WARNING,
  INFO
}

@JsonClass(generateAdapter = true)
data class AgriConfig(
  @Json(name = "onBelow") val onBelow: Double = 35.0,
  @Json(name = "offAbove") val offAbove: Double = 60.0,
  @Json(name = "minTank") val minTank: Double = 15.0,
  @Json(name = "maxRunSec") val maxRunSec: Double = 300.0,
  @Json(name = "cooldownSec") val cooldownSec: Double = 120.0,
  @Json(name = "minFlow") val minFlow: Double = 0.2,
  @Json(name = "soilDry") val soilDry: Double = 2800.0,
  @Json(name = "soilWet") val soilWet: Double = 1200.0,
  @Json(name = "nutrLowPpm") val nutrLowPpm: Double = 400.0,
  @Json(name = "nutrTarget") val nutrTarget: Double = 1000.0
)

@JsonClass(generateAdapter = true)
data class AgriNutrientStatus(
  @Json(name = "en") val en: Boolean = false,
  @Json(name = "ppm") val ppm: Int = 0,
  @Json(name = "pct") val pct: Double = 0.0,
  @Json(name = "raw") val raw: Int = 0,
  @Json(name = "low") val low: Boolean = false,
  @Json(name = "reliable") val reliable: Boolean = true,
  @Json(name = "seq") val seq: Int = 0,
  @Json(name = "push") val push: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AgriStatus(
  @Json(name = "soil") val soil: Double = 50.0,
  @Json(name = "soilRaw") val soilRaw: Int = 1950,
  @Json(name = "tank") val tank: Double = 80.0,
  @Json(name = "tankRaw") val tankRaw: Int = 1750,
  @Json(name = "flow") val flow: Double = 0.0,
  @Json(name = "total") val total: Double = 0.0,
  @Json(name = "pump") val pump: Boolean = false,
  @Json(name = "mode") val mode: String = "auto", // "auto" or "manual"
  @Json(name = "fault") val fault: String = "",
  @Json(name = "runSec") val runSec: Int = 0,
  @Json(name = "cooldownSec") val cooldownSec: Int = 0,
  @Json(name = "waterLeft") val waterLeft: Int = 0,
  @Json(name = "hold") val hold: Int = 0,
  @Json(name = "uptime") val uptime: Long = 0,
  @Json(name = "rssi") val rssi: Int = -55,
  @Json(name = "cfg") val cfg: AgriConfig = AgriConfig(),
  @Json(name = "nutr") val nutr: AgriNutrientStatus? = null
)

@JsonClass(generateAdapter = true)
data class TestAlertResponse(
  @Json(name = "ok") val ok: Boolean = false,
  @Json(name = "configured") val configured: Boolean = false
)

data class AgriAlert(
  val id: Long = System.currentTimeMillis(),
  val timestamp: Long = System.currentTimeMillis(),
  val level: AlertLevel = AlertLevel.INFO,
  val title: String,
  val message: String,
  val isRead: Boolean = false
)

data class AgriHistoryPoint(
  val timestamp: Long,
  val soil: Double,
  val food: Double?,
  val tank: Double,
  val flow: Double
)

data class ActiveBanner(
  val level: AlertLevel,
  val iconName: String,
  val title: String,
  val description: String,
  val primaryAction: String? = null,
  val primaryActionLabel: String? = null
)
