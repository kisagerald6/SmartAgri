package com.example.data.model

enum class CropGrowthStage(val displayName: String, val progressPct: Int) {
  GERMINATION("Germination / Seedling", 15),
  VEGETATIVE("Vegetative Canopy", 40),
  FLOWERING("Flowering & Fruit Set", 70),
  FRUIT_DEVELOPMENT("Fruit Bulking & Maturation", 85),
  HARVEST_READY("Harvest Ready", 100)
}

enum class SoilType(val displayName: String, val waterRetention: String, val nutrientRetention: String) {
  SANDY_LOAM("Sandy Loam", "Moderate", "Moderate"),
  CLAY_LOAM("Clay Loam", "High", "High"),
  SILT_LOAM("Silt Loam", "High", "Moderate-High"),
  HYDRO_COCO("Hydroponic Coco Coir", "Precise / Rapid Drain", "Controlled Fertigation"),
  ROCKWOOL("Rockwool Slab", "Fast Response", "Full Hydro Control")
}

data class YieldForecastInput(
  val cropName: String = "Commercial Greenhouse Tomato (Indeterminate)",
  val zoneId: String = "zone_1",
  val plantingDateEpoch: Long = System.currentTimeMillis() - (45L * 24 * 3600 * 1000), // 45 days ago
  val areaSqMeters: Double = 1000.0,
  val plantCount: Int = 2800,
  val growthStage: CropGrowthStage = CropGrowthStage.FLOWERING,
  val soilType: SoilType = SoilType.HYDRO_COCO,
  val avgDayTempC: Double = 24.5,
  val avgNightTempC: Double = 18.2,
  val avgRelativeHumidityPct: Double = 68.0,
  val dailyLightIntegral: Double = 22.0, // mol/m²/day
  val avgSoilMoisturePct: Double = 58.0,
  val avgNutrientsPpm: Double = 850.0
)

data class YieldRiskFactor(
  val title: String,
  val severity: AlertLevel,
  val impactPct: Double,
  val mitigationRecommendation: String
)

data class YieldForecastResult(
  val cropName: String,
  val predictedYieldKg: Double,
  val predictedYieldPerSqM: Double,
  val baselinePotentialKg: Double,
  val harvestWindowStart: Long,
  val harvestWindowEnd: Long,
  val daysUntilHarvest: Int,
  val potentialScorePct: Int, // 0 - 100%
  val riskFactors: List<YieldRiskFactor>,
  val actionItems: List<String>,
  val growingDegreeDaysAccumulated: Double,
  val timestamp: Long = System.currentTimeMillis()
)
