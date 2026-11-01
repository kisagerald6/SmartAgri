package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.DiseaseSeverity
import com.example.data.model.PestDiseaseDiagnosis
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "PestDetectionAi"

class PestDetectionAiService {

  private val diagnosisSchema = Schema.obj(
    properties = mapOf(
      "cropName" to Schema.string("Identified plant or crop species, e.g. Tomato, Bell Pepper, Cucumber, Lettuce"),
      "diagnosis" to Schema.string("Identified pest or disease, e.g. Early Blight, Powdery Mildew, Spider Mites, Blossom End Rot, or Healthy"),
      "confidenceScore" to Schema.double("Confidence score between 0.0 and 1.0"),
      "severity" to Schema.enumeration(
        values = listOf("LOW", "MODERATE", "HIGH", "CRITICAL"),
        description = "Disease or infestation severity level"
      ),
      "affectedPart" to Schema.string("Specific plant organ affected, e.g. Lower leaves, Petioles, Stem, Fruit"),
      "symptomsObserved" to Schema.array(
        items = Schema.string(),
        description = "List of visual pathological symptoms observed on the crop"
      ),
      "organicTreatments" to Schema.array(
        items = Schema.string(),
        description = "Organic, biological or cultural treatments (e.g. Neem oil, Bacillus subtilis, beneficial insects, sanitation)"
      ),
      "chemicalTreatments" to Schema.array(
        items = Schema.string(),
        description = "Targeted chemical treatment options with active ingredients, application methods and precautions"
      ),
      "preventativeMeasures" to Schema.array(
        items = Schema.string(),
        description = "Long-term agronomic prevention strategies (irrigation timing, greenhouse humidity control, spacing)"
      )
    )
  )

  private val model by lazy {
    Firebase.ai.generativeModel(
      modelName = "gemini-3.8-flash",
      generationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = diagnosisSchema
      }
    )
  }

  suspend fun analyzeCropImage(bitmap: Bitmap, imageUri: String?): Result<PestDiseaseDiagnosis> {
    return try {
      val prompt = """
        You are an expert commercial agricultural pathologist and greenhouse crop consultant.
        Analyze this crop leaf / foliage image thoroughly for any pests, fungal pathogens, bacterial infections, viral symptoms, or physiological nutrient deficiencies.
        Provide a rigorous diagnosis with confidence, severity level, observed symptoms, practical organic/biological treatments, safe commercial chemical options with precautions, and prevention strategies.
        If the plant appears completely healthy, clearly note Healthy with low severity and routine preventive advice.
      """.trimIndent()

      val response = withTimeout(60_000L) {
        model.generateContent(
          content {
            text(prompt)
            image(bitmap)
          }
        )
      }

      val jsonString = response.text ?: throw IllegalStateException("Model returned empty text.")
      val json = JSONObject(jsonString)

      val cropName = json.optString("cropName", "Greenhouse Crop")
      val diagnosis = json.optString("diagnosis", "Unknown Crop Anomaly")
      val confidence = json.optDouble("confidenceScore", 0.90)
      val severityStr = json.optString("severity", "MODERATE")
      val severity = try {
        DiseaseSeverity.valueOf(severityStr.uppercase())
      } catch (e: Exception) {
        DiseaseSeverity.MODERATE
      }
      val affectedPart = json.optString("affectedPart", "Foliage")

      val symptoms = mutableListOf<String>()
      val symArr = json.optJSONArray("symptomsObserved")
      if (symArr != null) {
        for (i in 0 until symArr.length()) symptoms.add(symArr.getString(i))
      }

      val organic = mutableListOf<String>()
      val orgArr = json.optJSONArray("organicTreatments")
      if (orgArr != null) {
        for (i in 0 until orgArr.length()) organic.add(orgArr.getString(i))
      }

      val chemical = mutableListOf<String>()
      val chemArr = json.optJSONArray("chemicalTreatments")
      if (chemArr != null) {
        for (i in 0 until chemArr.length()) chemical.add(chemArr.getString(i))
      }

      val preventative = mutableListOf<String>()
      val prevArr = json.optJSONArray("preventativeMeasures")
      if (prevArr != null) {
        for (i in 0 until prevArr.length()) preventative.add(prevArr.getString(i))
      }

      val alertTriggered = severity == DiseaseSeverity.HIGH || severity == DiseaseSeverity.CRITICAL

      Result.success(
        PestDiseaseDiagnosis(
          cropName = cropName,
          diagnosis = diagnosis,
          confidenceScore = confidence,
          severity = severity,
          affectedPart = affectedPart,
          symptomsObserved = symptoms,
          organicTreatments = organic,
          chemicalTreatments = chemical,
          preventativeMeasures = preventative,
          alertTriggered = alertTriggered,
          timestamp = System.currentTimeMillis(),
          imageUri = imageUri
        )
      )
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Crop pest analysis timed out", e)
      Result.failure(Exception("AI analysis timed out after 60s. Check connection and retry."))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Crop pest analysis failed", e)
      Result.failure(e)
    }
  }
}
