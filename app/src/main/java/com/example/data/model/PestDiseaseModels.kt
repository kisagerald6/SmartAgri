package com.example.data.model

import com.squareup.moshi.JsonClass

enum class DiseaseSeverity {
  LOW,
  MODERATE,
  HIGH,
  CRITICAL
}

@JsonClass(generateAdapter = true)
data class PestDiseaseDiagnosis(
  val cropName: String,
  val diagnosis: String,
  val confidenceScore: Double,
  val severity: DiseaseSeverity,
  val affectedPart: String,
  val symptomsObserved: List<String>,
  val organicTreatments: List<String>,
  val chemicalTreatments: List<String>,
  val preventativeMeasures: List<String>,
  val alertTriggered: Boolean = false,
  val timestamp: Long = System.currentTimeMillis(),
  val imageUri: String? = null
)

data class SampleCropImage(
  val id: String,
  val title: String,
  val cropType: String,
  val description: String,
  val sampleDiagnosis: PestDiseaseDiagnosis
)
