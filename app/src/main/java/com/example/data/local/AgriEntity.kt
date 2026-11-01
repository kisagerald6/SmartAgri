package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agri_alerts")
data class AgriAlertEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val level: String, // CRITICAL, WARNING, INFO
  val title: String,
  val message: String,
  val isRead: Boolean
)

@Entity(tableName = "agri_zones")
data class FarmZoneEntity(
  @PrimaryKey val zoneId: String,
  val name: String,
  val hostAddress: String,
  val isDemo: Boolean,
  val cropType: String,
  val areaSqMeters: Double = 1000.0,
  val targetMoisture: Double = 50.0,
  val targetNutrientsPpm: Double = 1000.0,
  val isSelected: Boolean = false
)

@Entity(tableName = "pest_scans")
data class PestScanEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val cropName: String,
  val diagnosis: String,
  val confidenceScore: Double,
  val severity: String,
  val affectedPart: String,
  val symptomsJson: String,
  val organicTreatmentsJson: String,
  val chemicalTreatmentsJson: String,
  val preventativeJson: String,
  val imageUri: String?
)

@Entity(tableName = "yield_forecasts")
data class YieldForecastEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long,
  val cropName: String,
  val zoneId: String,
  val predictedYieldKg: Double,
  val harvestWindowStart: Long,
  val harvestWindowEnd: Long,
  val potentialScorePct: Int
)
