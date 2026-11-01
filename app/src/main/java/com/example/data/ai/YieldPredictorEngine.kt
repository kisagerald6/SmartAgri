package com.example.data.ai

import com.example.data.model.AlertLevel
import com.example.data.model.CropGrowthStage
import com.example.data.model.SoilType
import com.example.data.model.YieldForecastInput
import com.example.data.model.YieldForecastResult
import com.example.data.model.YieldRiskFactor
import kotlin.math.max
import kotlin.math.min

object YieldPredictorEngine {

  fun calculateForecast(input: YieldForecastInput): YieldForecastResult {
    val now = System.currentTimeMillis()
    val daysAfterPlanting = max(1, ((now - input.plantingDateEpoch) / (24 * 3600 * 1000L)).toInt())

    // Baseline yield potential per m² for commercial high-yield varieties
    val baselineYieldPerSqM = when {
      input.cropName.contains("Tomato", ignoreCase = true) -> 12.5 // kg/m² typical commercial greenhouse
      input.cropName.contains("Pepper", ignoreCase = true) -> 8.0
      input.cropName.contains("Cucumber", ignoreCase = true) -> 18.0
      input.cropName.contains("Lettuce", ignoreCase = true) || input.cropName.contains("Green", ignoreCase = true) -> 4.5
      input.cropName.contains("Strawberry", ignoreCase = true) -> 5.5
      else -> 9.0
    }

    val totalBaselinePotentialKg = baselineYieldPerSqM * input.areaSqMeters

    // 1. Temperature & Growing Degree Days (GDD) Factor
    // Base temperature typically 10°C for warm season crops (tomatoes/peppers)
    val baseTemp = 10.0
    val meanDailyTemp = (input.avgDayTempC + input.avgNightTempC) / 2.0
    val dailyGdd = max(0.0, meanDailyTemp - baseTemp)
    val accumulatedGdd = dailyGdd * daysAfterPlanting

    var tempFactor = 1.0
    val riskFactors = mutableListOf<YieldRiskFactor>()
    val actionItems = mutableListOf<String>()

    if (input.avgDayTempC > 32.0) {
      tempFactor *= 0.88
      riskFactors.add(
        YieldRiskFactor(
          title = "High Temperature Stress (${input.avgDayTempC.toInt()}°C)",
          severity = AlertLevel.WARNING,
          impactPct = 12.0,
          mitigationRecommendation = "Activate greenhouse evaporative cooling pads and shading screens to protect pollen viability."
        )
      )
      actionItems.add("Deploy 30% thermal shade screens during peak 11:00-15:00 solar hours.")
    } else if (input.avgNightTempC < 14.0) {
      tempFactor *= 0.92
      riskFactors.add(
        YieldRiskFactor(
          title = "Low Night Temperature (${input.avgNightTempC.toInt()}°C)",
          severity = AlertLevel.WARNING,
          impactPct = 8.0,
          mitigationRecommendation = "Maintain minimum night heating above 16°C to avoid slowed fruit bulking."
        )
      )
      actionItems.add("Check greenhouse heating circuits and curtain seals for night insulation.")
    }

    // 2. Light / Daily Light Integral (DLI) Factor
    // Target DLI for fruiting crops is 18 - 25 mol/m²/day
    var lightFactor = 1.0
    if (input.dailyLightIntegral < 14.0) {
      lightFactor = 0.85
      riskFactors.add(
        YieldRiskFactor(
          title = "Low Light Integral (${input.dailyLightIntegral} mol/m²/d)",
          severity = AlertLevel.WARNING,
          impactPct = 15.0,
          mitigationRecommendation = "Supplemental LED photoperiod lighting recommended to reach 20+ mol/m²/day."
        )
      )
      actionItems.add("Activate inter-canopy supplemental LED lighting for 4 hours before dusk.")
    } else if (input.dailyLightIntegral >= 20.0) {
      lightFactor = 1.03 // Light boost bonus
    }

    // 3. Soil / Substrate Factor
    val soilFactor = when (input.soilType) {
      SoilType.HYDRO_COCO -> 1.05
      SoilType.ROCKWOOL -> 1.08
      SoilType.SILT_LOAM -> 1.00
      SoilType.SANDY_LOAM -> 0.96
      SoilType.CLAY_LOAM -> 0.94
    }

    // 4. Moisture & Irrigation Consistency Factor
    var moistureFactor = 1.0
    if (input.avgSoilMoisturePct < 40.0) {
      val deficit = (40.0 - input.avgSoilMoisturePct) * 0.015
      moistureFactor = max(0.70, 1.0 - deficit)
      riskFactors.add(
        YieldRiskFactor(
          title = "Sub-optimal Soil Moisture (${input.avgSoilMoisturePct.toInt()}%)",
          severity = AlertLevel.CRITICAL,
          impactPct = deficit * 100,
          mitigationRecommendation = "Increase daily automated irrigation pulse cycles by 20% to prevent blossom end rot and leaf drop."
        )
      )
      actionItems.add("Calibrate irrigation schedule to pulse 3 times daily during peak transpiration.")
    } else if (input.avgSoilMoisturePct > 80.0) {
      moistureFactor = 0.92
      riskFactors.add(
        YieldRiskFactor(
          title = "Root Zone Waterlogging (${input.avgSoilMoisturePct.toInt()}%)",
          severity = AlertLevel.WARNING,
          impactPct = 8.0,
          mitigationRecommendation = "Reduce pulse duration; risk of pythium root rot and nutrient lockout."
        )
      )
      actionItems.add("Check drainage runoff channels and extend irrigation rest intervals.")
    }

    // 5. Nutrient EC/TDS Factor
    var nutrientFactor = 1.0
    if (input.avgNutrientsPpm < 600.0) {
      nutrientFactor = 0.86
      riskFactors.add(
        YieldRiskFactor(
          title = "Nutrient Fertigation Deficit (${input.avgNutrientsPpm.toInt()} ppm)",
          severity = AlertLevel.WARNING,
          impactPct = 14.0,
          mitigationRecommendation = "Target EC/TDS is 800 - 1,200 ppm for fruiting stage. Increase NPK fertigation concentration."
        )
      )
      actionItems.add("Inject balanced potassium nitrate and calcium solution into the stock tank.")
    } else if (input.avgNutrientsPpm > 1800.0) {
      nutrientFactor = 0.94
      riskFactors.add(
        YieldRiskFactor(
          title = "High Salt Index (${input.avgNutrientsPpm.toInt()} ppm)",
          severity = AlertLevel.WARNING,
          impactPct = 6.0,
          mitigationRecommendation = "Flush lines with fresh water to prevent root tip burn and osmotic stress."
        )
      )
    }

    // Growth Stage Multiplier
    val overallMultiplier = min(1.15, max(0.50, tempFactor * lightFactor * soilFactor * moistureFactor * nutrientFactor))
    val predictedYieldKg = totalBaselinePotentialKg * overallMultiplier
    val predictedYieldPerSqM = predictedYieldKg / input.areaSqMeters
    val potentialScorePct = ((predictedYieldKg / totalBaselinePotentialKg) * 100).toInt().coerceIn(40, 100)

    // Estimate Harvest Window
    // Typical days to harvest from planting: 75-100 days for tomatoes
    val standardMaturityDays = when {
      input.cropName.contains("Tomato", ignoreCase = true) -> 85
      input.cropName.contains("Pepper", ignoreCase = true) -> 90
      input.cropName.contains("Cucumber", ignoreCase = true) -> 60
      input.cropName.contains("Lettuce", ignoreCase = true) -> 45
      else -> 75
    }

    val daysRemaining = max(3, standardMaturityDays - daysAfterPlanting)
    val harvestStart = now + (daysRemaining * 24 * 3600 * 1000L)
    val harvestEnd = harvestStart + (10L * 24 * 3600 * 1000L) // 10-day peak window

    if (riskFactors.isEmpty()) {
      actionItems.add("Current environmental and fertigation parameters are optimal for maximum commercial harvest.")
      actionItems.add("Maintain steady automated irrigation thresholds (Start 35%, Stop 60%).")
    }

    return YieldForecastResult(
      cropName = input.cropName,
      predictedYieldKg = predictedYieldKg,
      predictedYieldPerSqM = predictedYieldPerSqM,
      baselinePotentialKg = totalBaselinePotentialKg,
      harvestWindowStart = harvestStart,
      harvestWindowEnd = harvestEnd,
      daysUntilHarvest = daysRemaining,
      potentialScorePct = potentialScorePct,
      riskFactors = riskFactors,
      actionItems = actionItems,
      growingDegreeDaysAccumulated = accumulatedGdd,
      timestamp = now
    )
  }
}
